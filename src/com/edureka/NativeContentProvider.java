package com.edureka;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;

import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;

import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NativeContentProvider extends Activity {

	EditText etName, etManDate, etColor;
	TextView tvGarage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nativecontentprovider);

		Button view = (Button) findViewById(R.id.viewButton);
		Button add = (Button) findViewById(R.id.createButton);
		Button modify = (Button) findViewById(R.id.updateButton);
		Button delete = (Button) findViewById(R.id.deleteButton);

		etName = (EditText) findViewById(R.id.etName);
		etManDate = (EditText) findViewById(R.id.etManDate);
		etColor = (EditText) findViewById(R.id.etColor);

		tvGarage = (TextView) findViewById(R.id.tvContactsText);

		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				displayContacts();
				Log.i("NativeContentProvider",
						"Completed Displaying Contact list");
			}
		});

		add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				String name = etName.getText().toString();
				String manDate = etManDate.getText().toString();
				String color = etColor.getText().toString();
				
				if (name.equals("") && manDate.equals("") && color.equals("")) {
					Toast.makeText(getApplicationContext(), "Fields are empty",
							Toast.LENGTH_SHORT).show();
					return;
				}

				createContact(name, manDate, color);
				Log.i("NativeContentProvider",
						"Created a new contact, of course hard-coded");
			}
		});

		modify.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				String name = etName.getText().toString();
				String manDate = etManDate.getText().toString();
				String color = etColor.getText().toString();
				
				if (name.equals("") && manDate.equals("") && color.equals("")) {
					Toast.makeText(getApplicationContext(), "Fields are empty",
							Toast.LENGTH_SHORT).show();
					return;
				}

				updateContact(name, manDate, color);
				Log.i("NativeContentProvider",
						"Completed updating the email id, if applicable");
			}
		});

		delete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				String name = etName.getText().toString();

				if (name.equals("")) {
					Toast.makeText(getApplicationContext(),
							"Name Field is empty", Toast.LENGTH_SHORT).show();
					return;
				}

				deleteContact(name);
				Log.i("NativeContentProvider",
						"Deleted the just created contact");
			}
		});
	}

	private void displayContacts() {

		// This class provides applications access to the content model.
		ContentResolver contentresolver = getContentResolver();

		// A cursor is required to store the results from the database query
		Cursor cursor = contentresolver.query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		int count = cursor.getCount();

		if (count > 0) {
			String contactDetails = "";

			while (cursor.moveToNext()) {

				String columnId = ContactsContract.Contacts._ID;
				int cursorIndex = cursor.getColumnIndex(columnId);

				String id = cursor.getString(cursorIndex);

				String name = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				int numCount = Integer
						.parseInt(cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

				if (numCount > 0) {
					Cursor phoneCursor = contentresolver.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);

					while (phoneCursor.moveToNext()) {
						String phoneNo = phoneCursor
								.getString(phoneCursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

						contactDetails += "Name: " + name + ", Manufacturing Date: "
								+ phoneNo + "\n";

					}

					phoneCursor.close();
				}
			}
			tvGarage.setText(contactDetails);
		}
	}

	private void createContact(String name, String manDate, String color) {

		Cursor cursor = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		int count = cursor.getCount();

		if (count > 0) {
			while (cursor.moveToNext()) {

				String display_name = ContactsContract.Contacts.DISPLAY_NAME;
				int colIndex = cursor.getColumnIndex(display_name);
				String existName = cursor.getString(colIndex);

				if (existName.equals(name)) {
					Toast.makeText(NativeContentProvider.this,
							"The car: " + name + " already exists",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
		}

		// Operation
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = ops.size();

		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, null)
				.withValue(RawContacts.ACCOUNT_NAME, null).build());
		ops.add(ContentProviderOperation
				.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
				.withValue(StructuredName.DISPLAY_NAME, name).build());
		ops.add(ContentProviderOperation
				.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex)
				.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				.withValue(Phone.NUMBER, manDate)
				.withValue(Phone.NUMBER, color)
				.withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());

		try {
			getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Toast.makeText(
				NativeContentProvider.this,
				"Created a new contact with name: " + name + ", Manufacturing Date: "
						+ manDate + " and Color: " + color, Toast.LENGTH_SHORT).show();

	}

	private void updateContact(String name, String manDate, String color) {

		ContentResolver cr = getContentResolver();

		String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
		String[] params = new String[] { name };

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		ops.add(ContentProviderOperation
				.newUpdate(ContactsContract.Data.CONTENT_URI)
				.withSelection(where, params)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, manDate)
				.build());
		ops.add(ContentProviderOperation
				.newUpdate(ContactsContract.Data.CONTENT_URI)
				.withSelection(where, params)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, color)
				.build());

		try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Toast.makeText(NativeContentProvider.this,
				"Updated the manufacturing date of '" + name + "' to: " + manDate,
				Toast.LENGTH_SHORT).show();
	}

	private void deleteContact(String name) {

		ContentResolver cr = getContentResolver();
		String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
		String[] params = new String[] { name };

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation
				.newDelete(ContactsContract.RawContacts.CONTENT_URI)
				.withSelection(where, params).build());
		try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Toast.makeText(NativeContentProvider.this,
				"Deleted the contact with name '" + name + "'",
				Toast.LENGTH_SHORT).show();

	}
}
