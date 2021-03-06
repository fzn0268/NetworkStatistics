package fzn.projects.networkstatistics;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * 通知
 * 网络状态通知
 * Helper class for showing and canceling net speed notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class MeterNotification {
	/**
	 * The unique identifier for this type of notification.
	 */
	protected static final String TAG = MeterNotification.class.getSimpleName();

	/**
	 * Shows the notification, or updates a previously shown notification of
	 * this type, with the given parameters.
	 * <p>
	 * TODO: Customize this method's arguments to present relevant content in
	 * the notification.
	 * <p>
	 * TODO: Customize the contents of this method to tweak the behavior and
	 * presentation of net speed notifications. Make sure to follow the <a
	 * href="https://developer.android.com/design/patterns/notifications.html">
	 * Notification design guidelines</a> when doing so.
	 *
	 * @see #cancel(Context)
	 */
	public static Notification notify(@android.support.annotation.NonNull final Context context,
									  final String tickerString, final Intent intent) {
		final Resources res = context.getResources();

		// This image is used as the notification's large icon (thumbnail).
		// TODO: Remove this if your notification has no relevant thumbnail.

		//final String title = res.getString(R.string.net_speed_notification_rate, titleString);
		//final String text = res.getString(R.string.net_speed_notification_used, textString);
		
		final RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.meter_notification);
		//contentView.setTextViewText(R.id.notificationRate, title);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)

				// Set appropriate defaults for the notification light, sound,
				// and vibration.
				// .setDefaults(Notification.DEFAULT_ALL)

				// Set required fields, including the small icon, the
				// notification title, and text.
				.setSmallIcon(R.drawable.ic_stat_net_speed)
				// .setContentTitle(title).setContentText(text)
				
				// Set a custom view
				.setContent(contentView)

				// All fields below this line are optional.

				// Use a default priority (recognized on devices running Android
				// 4.1 or later)
				.setPriority(NotificationCompat.PRIORITY_MAX)

				// Provide a large icon, shown with the notification in the
				// notification drawer on devices running Android 3.0 or later.
				// .setLargeIcon(picture)

				// Set ticker text (preview) information for this notification.
				.setTicker(tickerString)

				// Show a number. This is useful when stacking notifications of
				// a single type.
				// .setNumber(...)

				// If this notification relates to a past or upcoming event, you
				// should set the relevant time information using the setWhen
				// method below. If this call is omitted, the notification's
				// timestamp will by set to the time at which it was shown.
				// TODO: Call setWhen if this notification relates to a past or
				// upcoming event. The sole argument to this method should be
				// the notification timestamp in milliseconds.
				// .setWhen(...)

				// Set the pending intent to be initiated when the user touches
				// the notification.
				.setContentIntent(
						PendingIntent.getActivity(
								context,
								0,
								intent,
								PendingIntent.FLAG_UPDATE_CURRENT))

				// Automatically dismiss the notification when it is touched.
				.setAutoCancel(false)
				.setOngoing(true);

		//notify(context, builder.build());
		return builder.build();
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static void notify(@android.support.annotation.NonNull final Context context,
							   @android.support.annotation.NonNull final Notification notification) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			nm.notify(TAG, 0, notification);
		} else {
			nm.notify(TAG.hashCode(), notification);
		}
	}

	/**
	 * Cancels any notifications of this type previously shown using
	 * {@link #notify(Context, String, int)}.
	 */
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static void cancel(@android.support.annotation.NonNull final Context context) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			nm.cancel(TAG, 0);
		} else {
			nm.cancel(TAG.hashCode());
		}
	}

}