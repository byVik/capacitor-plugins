package com.byvik.capacitor.playgames;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * Google Play Games Services v2: sign-in, leaderboards, achievements and saved games.
 */
@CapacitorPlugin(name = "PlayGames")
public class PlayGamesPlugin extends Plugin {

    private static final String TAG = "CapacitorPlayGames";

    // The Play Games app is where a Google account is added and a player profile is created.
    private static final String PLAY_GAMES_PACKAGE = "com.google.android.play.games";

    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_ACHIEVEMENTS_UI = 9005;

    @Override
    public void load() {
        // Initializing the v2 SDK also starts its silent sign-in attempt. It only succeeds when the
        // player already has a Play Games profile and has not declined before, which is why
        // isAuthenticated() and signIn() exist.
        try {
            PlayGamesSdk.initialize(getContext());
        } catch (Exception e) {
            // A missing or wrong setup must not take the whole app down at launch.
            Log.e(TAG, "PlayGamesSdk.initialize failed. Is com.google.android.gms.games.APP_ID in the manifest?", e);
        }
    }

    // ---------- Session ----------

    @PluginMethod
    public void isAuthenticated(PluginCall call) {
        PlayGames.getGamesSignInClient(getActivity())
            .isAuthenticated()
            .addOnCompleteListener((task) -> call.resolve(authResult("isAuthenticated", task)));
    }

    // The only SDK call that shows Google UI. This is where a player with a Google account but no
    // Play Games profile gets to create one.
    @PluginMethod
    public void signIn(PluginCall call) {
        PlayGames.getGamesSignInClient(getActivity())
            .signIn()
            .addOnCompleteListener((task) -> call.resolve(authResult("signIn", task)));
    }

    // Neither session call rejects: "it failed" and "the player said no" both mean the game carries
    // on signed out. The status code is what tells a declined prompt (16, 12501) apart from a
    // misconfigured app (10: this build's SHA-1 or package name is missing from the OAuth
    // credentials, so it will never work for anyone until the Play Console is fixed).
    private JSObject authResult(String method, Task<AuthenticationResult> task) {
        boolean authenticated = task.isSuccessful() && task.getResult() != null && task.getResult().isAuthenticated();
        JSObject ret = new JSObject();
        ret.put("isAuthenticated", authenticated);
        Exception e = task.getException();
        if (!authenticated && e != null) {
            String message = String.valueOf(e.getMessage());
            if (e instanceof ApiException) {
                ret.put("errorCode", ((ApiException) e).getStatusCode());
            }
            ret.put("errorMessage", message);
            Log.w(TAG, method + " failed: " + message, e);
        }
        return ret;
    }

    @PluginMethod
    public void getCurrentPlayer(PluginCall call) {
        PlayGames.getPlayersClient(getActivity())
            .getCurrentPlayer()
            .addOnSuccessListener((player) -> {
                JSObject ret = new JSObject();
                ret.put("playerId", player.getPlayerId());
                ret.put("displayName", player.getDisplayName());
                ret.put("title", player.getTitle());
                call.resolve(ret);
            })
            .addOnFailureListener((e) -> reject(call, e));
    }

    // For when signIn() cannot help: the device has no Google account at all. The Play Games app
    // can add one and create the profile; back in the game, the silent sign-in then succeeds.
    // Falls back to the store listing when the app is not installed (or not visible to us).
    @PluginMethod
    public void openPlayGamesApp(PluginCall call) {
        Intent app = getContext().getPackageManager().getLaunchIntentForPackage(PLAY_GAMES_PACKAGE);
        if (app != null) {
            app.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                getContext().startActivity(app);
                call.resolve();
                return;
            } catch (Exception e) {
                // Fall through to the store listing.
            }
        }
        if (
            openUri("market://details?id=" + PLAY_GAMES_PACKAGE) ||
            openUri("https://play.google.com/store/apps/details?id=" + PLAY_GAMES_PACKAGE)
        ) {
            call.resolve();
            return;
        }
        call.reject("Cannot open Play Games");
    }

    private boolean openUri(String uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- Leaderboards ----------

    @PluginMethod
    public void submitScore(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        Long score = longArg(call, "score");
        if (leaderboardId == null || score == null) {
            call.reject("leaderboardId and score are required");
            return;
        }
        if (Boolean.TRUE.equals(call.getBoolean("immediate", false))) {
            // Waits for the server, so a standing read right after this resolves sees the new score.
            PlayGames.getLeaderboardsClient(getActivity())
                .submitScoreImmediate(leaderboardId, score)
                .addOnSuccessListener((result) -> call.resolve())
                .addOnFailureListener((e) -> reject(call, e));
        } else {
            PlayGames.getLeaderboardsClient(getActivity()).submitScore(leaderboardId, score);
            call.resolve();
        }
    }

    // Rank, score and number of entries come from the same leaderboard variant, so they are read
    // in one call: fetched separately, a rank could be paired with a count from another moment.
    @PluginMethod
    public void getLeaderboardStanding(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        if (leaderboardId == null) {
            call.reject("leaderboardId is required");
            return;
        }
        int timeSpan = timeSpan(call);
        int collection = collection(call);
        boolean forceReload = Boolean.TRUE.equals(call.getBoolean("forceReload", false));
        PlayGames.getLeaderboardsClient(getActivity())
            .loadLeaderboardMetadata(leaderboardId, forceReload)
            .addOnSuccessListener((data) -> {
                // Play reports "unknown" as -1; it is surfaced as null instead.
                JSObject ret = new JSObject();
                ret.put("rank", JSONObject.NULL);
                ret.put("score", JSONObject.NULL);
                ret.put("numScores", JSONObject.NULL);
                Leaderboard leaderboard = data == null ? null : data.get();
                if (leaderboard != null && leaderboard.getVariants() != null) {
                    for (LeaderboardVariant variant : leaderboard.getVariants()) {
                        if (variant.getTimeSpan() != timeSpan || variant.getCollection() != collection) {
                            continue;
                        }
                        if (variant.getNumScores() != LeaderboardVariant.NUM_SCORES_UNKNOWN) {
                            ret.put("numScores", variant.getNumScores());
                        }
                        if (variant.hasPlayerInfo()) {
                            if (variant.getPlayerRank() != LeaderboardVariant.PLAYER_RANK_UNKNOWN) {
                                ret.put("rank", variant.getPlayerRank());
                            }
                            if (variant.getRawPlayerScore() != LeaderboardVariant.PLAYER_SCORE_UNKNOWN) {
                                ret.put("score", variant.getRawPlayerScore());
                            }
                        }
                        break;
                    }
                }
                call.resolve(ret);
            })
            .addOnFailureListener((e) -> reject(call, e));
    }

    @PluginMethod
    public void showLeaderboard(PluginCall call) {
        String leaderboardId = call.getString("leaderboardId");
        if (leaderboardId == null) {
            call.reject("leaderboardId is required");
            return;
        }
        PlayGames.getLeaderboardsClient(getActivity())
            .getLeaderboardIntent(leaderboardId, timeSpan(call), collection(call))
            .addOnSuccessListener((intent) -> {
                getActivity().startActivityForResult(intent, RC_LEADERBOARD_UI);
                call.resolve();
            })
            .addOnFailureListener((e) -> reject(call, e));
    }

    @PluginMethod
    public void showAllLeaderboards(PluginCall call) {
        PlayGames.getLeaderboardsClient(getActivity())
            .getAllLeaderboardsIntent()
            .addOnSuccessListener((intent) -> {
                getActivity().startActivityForResult(intent, RC_LEADERBOARD_UI);
                call.resolve();
            })
            .addOnFailureListener((e) -> reject(call, e));
    }

    private static int timeSpan(PluginCall call) {
        String value = call.getString("timeSpan", "allTime");
        if ("daily".equals(value)) {
            return LeaderboardVariant.TIME_SPAN_DAILY;
        }
        if ("weekly".equals(value)) {
            return LeaderboardVariant.TIME_SPAN_WEEKLY;
        }
        return LeaderboardVariant.TIME_SPAN_ALL_TIME;
    }

    private static int collection(PluginCall call) {
        return "friends".equals(call.getString("collection", "public"))
            ? LeaderboardVariant.COLLECTION_FRIENDS
            : LeaderboardVariant.COLLECTION_PUBLIC;
    }

    // ---------- Achievements ----------
    // The fire-and-forget variants are used on purpose: the SDK queues them while offline and
    // syncs later, so these calls resolve as soon as the request is handed over.

    @PluginMethod
    public void unlockAchievement(PluginCall call) {
        String achievementId = call.getString("achievementId");
        if (achievementId == null) {
            call.reject("achievementId is required");
            return;
        }
        PlayGames.getAchievementsClient(getActivity()).unlock(achievementId);
        call.resolve();
    }

    @PluginMethod
    public void revealAchievement(PluginCall call) {
        String achievementId = call.getString("achievementId");
        if (achievementId == null) {
            call.reject("achievementId is required");
            return;
        }
        PlayGames.getAchievementsClient(getActivity()).reveal(achievementId);
        call.resolve();
    }

    @PluginMethod
    public void incrementAchievement(PluginCall call) {
        String achievementId = call.getString("achievementId");
        Integer steps = call.getInt("steps");
        if (achievementId == null || steps == null) {
            call.reject("achievementId and steps are required");
            return;
        }
        PlayGames.getAchievementsClient(getActivity()).increment(achievementId, steps);
        call.resolve();
    }

    // Absolute progress rather than increments, for games whose local save is the source of truth.
    // Play ignores a value lower than the one it already has, so a reinstall never lowers a counter.
    @PluginMethod
    public void setAchievementSteps(PluginCall call) {
        String achievementId = call.getString("achievementId");
        Integer steps = call.getInt("steps");
        if (achievementId == null || steps == null) {
            call.reject("achievementId and steps are required");
            return;
        }
        PlayGames.getAchievementsClient(getActivity()).setSteps(achievementId, steps);
        call.resolve();
    }

    @PluginMethod
    public void showAchievements(PluginCall call) {
        PlayGames.getAchievementsClient(getActivity())
            .getAchievementsIntent()
            .addOnSuccessListener((intent) -> {
                getActivity().startActivityForResult(intent, RC_ACHIEVEMENTS_UI);
                call.resolve();
            })
            .addOnFailureListener((e) -> reject(call, e));
    }

    // ---------- Saved games ----------
    // Conflicts are resolved with "most recently modified": the plugin treats the cloud copy as a
    // mirror of the local save, not as a second timeline to merge.

    @PluginMethod
    public void saveGame(PluginCall call) {
        String name = call.getString("name");
        String data = call.getString("data");
        if (name == null || data == null) {
            call.reject("name and data are required");
            return;
        }
        String description = call.getString("description");
        SnapshotsClient client = PlayGames.getSnapshotsClient(getActivity());
        client
            .open(name, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
            .addOnSuccessListener((result) -> {
                Snapshot snapshot = result.getData();
                if (snapshot == null) {
                    call.reject("Unresolved snapshot conflict");
                    return;
                }
                snapshot.getSnapshotContents().writeBytes(data.getBytes(StandardCharsets.UTF_8));
                SnapshotMetadataChange.Builder change = new SnapshotMetadataChange.Builder();
                if (description != null) {
                    change.setDescription(description);
                }
                client
                    .commitAndClose(snapshot, change.build())
                    .addOnSuccessListener((metadata) -> call.resolve())
                    .addOnFailureListener((e) -> reject(call, e));
            })
            .addOnFailureListener((e) -> reject(call, e));
    }

    // Resolves with data = null when the saved game does not exist yet (a player's first launch),
    // so "nothing saved" is not confused with "could not read".
    @PluginMethod
    public void loadGame(PluginCall call) {
        String name = call.getString("name");
        if (name == null) {
            call.reject("name is required");
            return;
        }
        SnapshotsClient client = PlayGames.getSnapshotsClient(getActivity());
        client
            .open(name, false, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
            .addOnSuccessListener((result) -> {
                Snapshot snapshot = result.getData();
                if (snapshot == null) {
                    call.reject("Unresolved snapshot conflict");
                    return;
                }
                try {
                    String data = new String(snapshot.getSnapshotContents().readFully(), StandardCharsets.UTF_8);
                    JSObject ret = new JSObject();
                    ret.put("data", data);
                    call.resolve(ret);
                } catch (IOException e) {
                    reject(call, e);
                } finally {
                    client.discardAndClose(snapshot);
                }
            })
            .addOnFailureListener((e) -> {
                if (e instanceof ApiException && ((ApiException) e).getStatusCode() == GamesClientStatusCodes.SNAPSHOT_NOT_FOUND) {
                    JSObject ret = new JSObject();
                    ret.put("data", JSONObject.NULL);
                    call.resolve(ret);
                } else {
                    reject(call, e);
                }
            });
    }

    // ---------- Helpers ----------

    // PluginCall.getLong() only accepts values already parsed as Long, and JSON integers that fit in
    // an int are not.
    private static Long longArg(PluginCall call, String key) {
        Object value = call.getData().opt(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    // The Google API status code travels as the error code, so callers can branch on it.
    private void reject(PluginCall call, Exception e) {
        String code = e instanceof ApiException ? String.valueOf(((ApiException) e).getStatusCode()) : null;
        call.reject(String.valueOf(e.getMessage()), code, e);
    }
}
