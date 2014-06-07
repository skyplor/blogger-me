package com.sky.bloggerme.model;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultSortOrder;

@DatabaseTable
public class Label
{
	/** The id that is generated automatically in the table, which represents a unique label. */
	@DatabaseField(generatedId = true, columnName = BaseColumns._ID)
	@DefaultSortOrder
	private int _id;
	
	@DatabaseField
	private String name;

	/**
	 * @return the _id
	 */
	public int get_id()
	{
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(int _id)
	{
		this._id = _id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
