package com.sky.bloggerme.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * The Class DroidWriterEditText.
 */
//TODO: It always appends 2 extra newline at the end. And there's some problem with the bold/italic/underline at the last word
public class DroidWriterEditText extends EditText
{
	
	/** The Constant TAG. */
	public static final String TAG = "DroidWriter";
	
	/** The Constant STYLE_BOLD. */
	private static final int STYLE_BOLD = 0;
	
	/** The Constant STYLE_ITALIC. */
	private static final int STYLE_ITALIC = 1;
	
	/** The Constant STYLE_UNDERLINED. */
	private static final int STYLE_UNDERLINED = 2;
	
	/** The bold toggle. */
	private ToggleButton boldToggle;
	
	/** The italics toggle. */
	private ToggleButton italicsToggle;
	
	/** The underline toggle. */
	private ToggleButton underlineToggle;
	
	/** The image getter. */
	private Html.ImageGetter imageGetter;

	/**
	 * Instantiates a new droid writer edit text.
	 *
	 * @param context the context
	 */
	public DroidWriterEditText(Context context)
	{
		super(context);
		initialize();
	}

	/**
	 * Instantiates a new droid writer edit text.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public DroidWriterEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize();
	}

	/**
	 * Instantiates a new droid writer edit text.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @param defStyle the def style
	 */
	public DroidWriterEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize();
	}

	/**
	 * Initialize.
	 */
	private void initialize()
	{
		this.imageGetter = new Html.ImageGetter()
		{
			public Drawable getDrawable(String source)
			{
				return null;
			}
		};
		addTextChangedListener(new DWTextWatcher());
	}

	/**
	 * Toggle style.
	 *
	 * @param style the style
	 */
	private void toggleStyle(int style)
	{
		Log.v(TAG, "In toggleStyle, Style: " + style);
		int selectionStart = getSelectionStart();

		int selectionEnd = getSelectionEnd();

		if (selectionStart > selectionEnd)
		{
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}

		if (selectionEnd > selectionStart)
		{
			Spannable str = getText();
			boolean exists = false;

			switch (style)
			{
				case STYLE_BOLD:
					StyleSpan[] styleSpans = (StyleSpan[]) str.getSpans(selectionStart, selectionEnd, StyleSpan.class);

					for (int i = 0; i < styleSpans.length; i++)
					{
						if (styleSpans[i].getStyle() == android.graphics.Typeface.BOLD)
						{
							str.removeSpan(styleSpans[i]);
							exists = true;
						}

					}

					if (!exists)
					{
						str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), selectionStart, selectionEnd, 34);
					}

					setSelection(selectionStart, selectionEnd);
					break;
					
				case STYLE_ITALIC:
					StyleSpan[] styleSpans2 = (StyleSpan[]) str.getSpans(selectionStart, selectionEnd, StyleSpan.class);

					for (int i = 0; i < styleSpans2.length; i++)
					{
						if (styleSpans2[i].getStyle() == android.graphics.Typeface.ITALIC)
						{
							str.removeSpan(styleSpans2[i]);
							exists = true;
						}

					}

					if (!exists)
					{
						str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), selectionStart, selectionEnd, 34);
					}

					setSelection(selectionStart, selectionEnd);
					break;
					
				case STYLE_UNDERLINED:
					UnderlineSpan[] underSpan = (UnderlineSpan[]) str.getSpans(selectionStart, selectionEnd, UnderlineSpan.class);

					for (int i = 0; i < underSpan.length; i++)
					{
						str.removeSpan(underSpan[i]);
						exists = true;
					}

					if (!exists)
					{
						str.setSpan(new UnderlineSpan(), selectionStart, selectionEnd, 34);
					}

					setSelection(selectionStart, selectionEnd);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.TextView#onSelectionChanged(int, int)
	 */
	public void onSelectionChanged(int selStart, int selEnd)
	{
		Log.v(TAG, "In onSelectionChanged, SelStart: " + selStart + ", SelEnd: " + selEnd);
		boolean boldExists = false;
		boolean italicsExists = false;
		boolean underlinedExists = false;

		if ((selStart > 0) && (selStart == selEnd))
		{
			CharacterStyle[] styleSpans = (CharacterStyle[]) getText().getSpans(selStart - 1, selStart, CharacterStyle.class);

			for (int i = 0; i < styleSpans.length; i++)
			{
				if ((styleSpans[i] instanceof StyleSpan))
				{
					if (((StyleSpan) styleSpans[i]).getStyle() == 1)
					{
						boldExists = true;
					}
					else if (((StyleSpan) styleSpans[i]).getStyle() == 2)
					{
						italicsExists = true;
					}
					else if (((StyleSpan) styleSpans[i]).getStyle() == 3)
					{
						italicsExists = true;
						boldExists = true;
					}
				}
				else if ((styleSpans[i] instanceof UnderlineSpan))
				{
					underlinedExists = true;
				}
			}

		}
		else
		{
			CharacterStyle[] styleSpans = (CharacterStyle[]) getText().getSpans(selStart, selEnd, CharacterStyle.class);

			for (int i = 0; i < styleSpans.length; i++)
			{
				if ((styleSpans[i] instanceof StyleSpan))
				{
					if (((StyleSpan) styleSpans[i]).getStyle() == 1)
					{
						if ((getText().getSpanStart(styleSpans[i]) <= selStart) && (getText().getSpanEnd(styleSpans[i]) >= selEnd))
							boldExists = true;
					}
					else if (((StyleSpan) styleSpans[i]).getStyle() == 2)
					{
						if ((getText().getSpanStart(styleSpans[i]) <= selStart) && (getText().getSpanEnd(styleSpans[i]) >= selEnd))
							italicsExists = true;
					}
					else if ((((StyleSpan) styleSpans[i]).getStyle() == 3) && (getText().getSpanStart(styleSpans[i]) <= selStart) && (getText().getSpanEnd(styleSpans[i]) >= selEnd))
					{
						italicsExists = true;
						boldExists = true;
					}
				}
				else if (((styleSpans[i] instanceof UnderlineSpan)) && (getText().getSpanStart(styleSpans[i]) <= selStart) && (getText().getSpanEnd(styleSpans[i]) >= selEnd))
				{
					underlinedExists = true;
				}

			}

		}

		if (this.boldToggle != null)
		{
			if (boldExists)
				this.boldToggle.setChecked(true);
			else
			{
				this.boldToggle.setChecked(false);
			}
		}
		if (this.italicsToggle != null)
		{
			if (italicsExists)
				this.italicsToggle.setChecked(true);
			else
			{
				this.italicsToggle.setChecked(false);
			}
		}
		if (this.underlineToggle != null)
			if (underlinedExists)
				this.underlineToggle.setChecked(true);
			else
				this.underlineToggle.setChecked(false);
	}

	/**
	 * Gets the spanned text.
	 *
	 * @return the spanned text
	 */
	public Spanned getSpannedText()
	{
		return getText();
	}

	/**
	 * Sets the spanned text.
	 *
	 * @param text the new spanned text
	 */
	public void setSpannedText(Spanned text)
	{
		setText(text);
	}

	/**
	 * Gets the string text.
	 *
	 * @return the string text
	 */
	public String getStringText()
	{
		return getText().toString();
	}

	/**
	 * Sets the string text.
	 *
	 * @param text the new string text
	 */
	public void setStringText(String text)
	{
		setText(text);
	}

	/**
	 * Gets the text html.
	 *
	 * @return the text html
	 */
	public String getTextHTML()
	{
		return Html.toHtml(getText());
	}

	/**
	 * Sets the text html.
	 *
	 * @param text the new text html
	 */
	public void setTextHTML(String text)
	{
		setText(Html.fromHtml(text, this.imageGetter, null));
	}

	/**
	 * Sets the image getter.
	 *
	 * @param imageGetter the new image getter
	 */
	public void setImageGetter(Html.ImageGetter imageGetter)
	{
		this.imageGetter = imageGetter;
	}

	/**
	 * Sets the bold toggle button.
	 *
	 * @param button the new bold toggle button
	 */
	public void setBoldToggleButton(ToggleButton button)
	{
		this.boldToggle = button;

		this.boldToggle.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				DroidWriterEditText.this.toggleStyle(STYLE_BOLD);
			}
		});
	}

	/**
	 * Sets the italics toggle button.
	 *
	 * @param button the new italics toggle button
	 */
	public void setItalicsToggleButton(ToggleButton button)
	{
		this.italicsToggle = button;

		this.italicsToggle.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				DroidWriterEditText.this.toggleStyle(STYLE_ITALIC);
			}
		});
	}

	/**
	 * Sets the underline toggle button.
	 *
	 * @param button the new underline toggle button
	 */
	public void setUnderlineToggleButton(ToggleButton button)
	{
		this.underlineToggle = button;

		this.underlineToggle.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				DroidWriterEditText.this.toggleStyle(STYLE_UNDERLINED);
			}
		});
	}

//	public void setImageInsertButton(View button, final String imageResource)
//	{
//		button.setOnClickListener(new View.OnClickListener()
//		{
//			public void onClick(View v)
//			{
//				int position = Selection.getSelectionStart(DroidWriterEditText.this.getText());
//
//				Spanned e = Html.fromHtml("<img src=\"" + imageResource + "\">", DroidWriterEditText.this.imageGetter, null);
//
//				DroidWriterEditText.this.getText().insert(position, e);
//			}
//		});
//	}
//
//	public void setClearButton(View button)
//	{
//		button.setOnClickListener(new View.OnClickListener()
//		{
//			public void onClick(View v)
//			{
//				DroidWriterEditText.this.setText("");
//			}
//		});
//	}

	/**
 * The Class DWTextWatcher.
 */
private class DWTextWatcher implements TextWatcher
	{
		
		/**
		 * Instantiates a new DW text watcher.
		 */
		private DWTextWatcher()
		{
		}

		/* (non-Javadoc)
		 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
		 */
		public void afterTextChanged(Editable editable)
		{
			Log.v(TAG, "In afterTextChanged, Bold: " + boldToggle.isChecked() + ", Italics: " + italicsToggle.isChecked());
			int position = Selection.getSelectionStart(DroidWriterEditText.this.getText());
			if (position < 0)
			{
				position = 0;
			}

			if (position > 0)
			{
				CharacterStyle[] appliedStyles = (CharacterStyle[]) editable.getSpans(position - 1, position, CharacterStyle.class);

				StyleSpan currentBoldSpan = null;
				StyleSpan currentItalicSpan = null;
				UnderlineSpan currentUnderlineSpan = null;

				for (int i = 0; i < appliedStyles.length; i++)
				{
					if ((appliedStyles[i] instanceof StyleSpan))
					{
						if (((StyleSpan) appliedStyles[i]).getStyle() == 1)
						{
							currentBoldSpan = (StyleSpan) appliedStyles[i];
						}
						else if (((StyleSpan) appliedStyles[i]).getStyle() == 2)
						{
							currentItalicSpan = (StyleSpan) appliedStyles[i];
						}
					}
					else if ((appliedStyles[i] instanceof UnderlineSpan))
					{
						currentUnderlineSpan = (UnderlineSpan) appliedStyles[i];
					}

				}

				if (DroidWriterEditText.this.boldToggle != null)
				{
					if ((DroidWriterEditText.this.boldToggle.isChecked()) && (currentBoldSpan == null))
					{
						editable.setSpan(new StyleSpan(1), position - 1, position, 34);
					}
					else if ((!DroidWriterEditText.this.boldToggle.isChecked()) && (currentBoldSpan != null))
					{
						int boldStart = editable.getSpanStart(currentBoldSpan);
						int boldEnd = editable.getSpanEnd(currentBoldSpan);

						editable.removeSpan(currentBoldSpan);
						if (boldStart <= position - 1)
						{
							editable.setSpan(new StyleSpan(1), boldStart, position - 1, 34);
						}

						if (boldEnd > position)
						{
							editable.setSpan(new StyleSpan(1), position, boldEnd, 34);
						}

					}

				}

				if ((DroidWriterEditText.this.italicsToggle != null) && (DroidWriterEditText.this.italicsToggle.isChecked()) && (currentItalicSpan == null))
				{
					editable.setSpan(new StyleSpan(2), position - 1, position, 34);
				}
				else if ((DroidWriterEditText.this.italicsToggle != null) && (!DroidWriterEditText.this.italicsToggle.isChecked()) && (currentItalicSpan != null))
				{
					int italicStart = editable.getSpanStart(currentItalicSpan);
					int italicEnd = editable.getSpanEnd(currentItalicSpan);

					editable.removeSpan(currentItalicSpan);
					if (italicStart <= position - 1)
					{
						editable.setSpan(new StyleSpan(2), italicStart, position - 1, 34);
					}

					if (italicEnd > position)
					{
						editable.setSpan(new StyleSpan(2), position, italicEnd, 34);
					}

				}

				if ((DroidWriterEditText.this.underlineToggle != null) && (DroidWriterEditText.this.underlineToggle.isChecked()) && (currentUnderlineSpan == null))
				{
					editable.setSpan(new UnderlineSpan(), position - 1, position, 34);
				}
				else if ((DroidWriterEditText.this.underlineToggle != null) && (!DroidWriterEditText.this.underlineToggle.isChecked()) && (currentUnderlineSpan != null))
				{
					int underLineStart = editable.getSpanStart(currentUnderlineSpan);
					int underLineEnd = editable.getSpanEnd(currentUnderlineSpan);

					editable.removeSpan(currentUnderlineSpan);
					if (underLineStart <= position - 1)
					{
						editable.setSpan(new UnderlineSpan(), underLineStart, position - 1, 34);
					}

					if (underLineEnd > position)
						editable.setSpan(new UnderlineSpan(), position, underLineEnd, 34);
				}
			}
		}

		/* (non-Javadoc)
		 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
		 */
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		/* (non-Javadoc)
		 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
		 */
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
		}
	}
}