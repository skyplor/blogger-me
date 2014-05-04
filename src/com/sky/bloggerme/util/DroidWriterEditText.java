package com.sky.bloggerme.util;

import com.sky.bloggerme.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
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
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ToggleButton;

public class DroidWriterEditText extends EditText
{
	public static final String TAG = "DroidWriter";
	private static final int STYLE_BOLD = 0;
	private static final int STYLE_ITALIC = 1;
	private static final int STYLE_UNDERLINED = 2;
	private ToggleButton boldToggle;
	private ToggleButton italicsToggle;
	private ToggleButton underlineToggle;
	private Html.ImageGetter imageGetter;

	public DroidWriterEditText(Context context)
	{
		super(context);
		initialize();
	}

	public DroidWriterEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize();
	}

	public DroidWriterEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize();
	}

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

	public Spanned getSpannedText()
	{
		return getText();
	}

	public void setSpannedText(Spanned text)
	{
		setText(text);
	}

	public String getStringText()
	{
		return getText().toString();
	}

	public void setStringText(String text)
	{
		setText(text);
	}

	public String getTextHTML()
	{
		return Html.toHtml(getText());
	}

	public void setTextHTML(String text)
	{
		setText(Html.fromHtml(text, this.imageGetter, null));
	}

	public void setImageGetter(Html.ImageGetter imageGetter)
	{
		this.imageGetter = imageGetter;
	}

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

	private class DWTextWatcher implements TextWatcher
	{
		private DWTextWatcher()
		{
		}

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

		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
		}

		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
		}
	}
}