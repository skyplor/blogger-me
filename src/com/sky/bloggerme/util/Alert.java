package com.sky.bloggerme.util;

import android.R.drawable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * The Class Alert.
 */
public class Alert
{

	/**
	 * Show alert.
	 *
	 * @param con the context
	 * @param title the title
	 * @param message the message
	 */
	public static void showAlert(Context con, String title, String message)
	{
		Dialog dlg = new AlertDialog.Builder(con).setIcon(drawable.ic_dialog_alert).setTitle(title).setPositiveButton("OK", null).setMessage(message).create();
		dlg.show();
	}

	/**
	 * Show alert.
	 *
	 * @param con the context
	 * @param title the title
	 * @param message the message
	 * @param positiveButtontxt the text for positive buttontxt
	 * @param positiveListener the positive listener
	 * @param negativeButtontxt the negative buttontxt
	 * @param negativeListener the negative listener
	 */
	public static void showAlert(Context con, String title, String message, CharSequence positiveButtontxt, DialogInterface.OnClickListener positiveListener, CharSequence negativeButtontxt, DialogInterface.OnClickListener negativeListener)
	{
		Dialog dlg = new AlertDialog.Builder(con).setIcon(drawable.ic_dialog_alert).setTitle(title).setNegativeButton(negativeButtontxt, negativeListener).setPositiveButton(positiveButtontxt, positiveListener).setMessage(message).create();
		dlg.show();
	}
}
