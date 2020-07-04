import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class Hesabgar extends MIDlet {
	private static final String CATS_RS = "categories";

	private static final String TODAY_BILL_RS = "todaybill";

	private Display mDisplay;

	private Form mStartScreen;

	private TextBox mExpendAmountScreen;

	private List mExpendCatScreen;

	private Form mTodayExpendScreen;

	private Hashtable mExpendCats; // maps catIDs to cat names

	private int[] mExpendCatIDs;

	private Bill mTodayBill;

	private Alert mErrorScreen;

	private Alert mResetScreen;
	
	private boolean mIsEdited; //if todays bill is edited
	
	public Hesabgar() {
		// Generic commands
		final Command cmdBack = new Command(
				"\u0628\u0627\u0632\u06af\u0634\u062a", Command.BACK, 1);
		final Command cmdOK = new Command("\u0628\u0627\u0634\u062f",
				Command.OK, 0);

		// Start screen
		mStartScreen = new Form("\u062d\u0633\u0627\u0628\u06af\u0631");
		StringItem si = new StringItem("\u062c\u0645\u0639\u0020\u0647\u0632\u064a\u0646\u0647\u0020\u0647\u0627\u064a\u0020\u0627\u0645\u0631\u0648\u0632:", "");
		si.setLayout(Item.LAYOUT_2 | Item.LAYOUT_RIGHT);
		mStartScreen.append(si);
		// Start screen commands
		final Command cmdExit = new Command("\u062e\u0631\u0648\u062c",
				Command.EXIT, 1);
		final Command cmdNewExpend = new Command("\u062c\u062f\u064a\u062f",
				"\u0647\u0632\u064a\u0646\u0647\u0020\u062c\u062f\u064a\u062f",
				Command.SCREEN, 0);
		final Command cmdTodayExpend = new Command(
				"\u0627\u0645\u0631\u0648\u0632",
				"\u0647\u0632\u064a\u0646\u0647\u0020\u0647\u0627\u064a\u0020\u0627\u0645\u0631\u0648\u0632",
				Command.SCREEN, 1);
		final Command cmdReset = new Command("\u0627\u0632\u0020\u0646\u0648",
				Command.SCREEN, 1);

		mStartScreen.addCommand(cmdExit);
		mStartScreen.addCommand(cmdNewExpend);
		mStartScreen.addCommand(cmdTodayExpend);
		mStartScreen.addCommand(cmdReset);
		mStartScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command cmd, Displayable d) {
				if (cmd == cmdExit) {
					shutdown();
					notifyDestroyed();
				} else if (cmd == cmdNewExpend) {
					mExpendAmountScreen.setString("");
					mDisplay.setCurrent(mExpendAmountScreen);
				} else if (cmd == cmdTodayExpend) {
					updateTodayExpendsScreen();
					mDisplay.setCurrent(mTodayExpendScreen);
				} else if (cmd == cmdReset) {
					mDisplay.setCurrent(mResetScreen);
				}
			}
		});

		// Error screen
		mErrorScreen = new Alert("\u062e\u0637\u0627", "", null,
				AlertType.ERROR);
		mErrorScreen.setTimeout(Alert.FOREVER);
		mErrorScreen.addCommand(cmdExit);
		mErrorScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command cmd, Displayable d) {
				if (cmd == cmdExit) {
					shutdown();
					notifyDestroyed();
				}
			}
		});

		// Reset screen
		mResetScreen = new Alert(
				"\u0633\u0624\u0627\u0644",
				"\u0622\u064a\u0627\u0020\u0645\u0637\u0645\u0626\u0646\u064a\u062f\u0020\u0643\u0647\u0020\u0645\u064a\u0020\u062e\u0648\u0627\u0647\u064a\u062f\u0020\u0647\u0645\u0647\u0020\u0627\u0637\u0644\u0627\u0639\u0627\u062a\u0020\u067e\u0627\u0643\u0020\u0634\u0648\u0646\u062f\u061f",
				null, AlertType.CONFIRMATION);
		mResetScreen.setTimeout(Alert.FOREVER);
		final Command cmdYes = new Command("\u0628\u0644\u0647",
				Command.SCREEN, 1);
		final Command cmdNo = new Command("\u0646\u0647", Command.SCREEN, 0);
		mResetScreen.addCommand(cmdYes);
		mResetScreen.addCommand(cmdNo);
		mResetScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == cmdYes) {
					mTodayBill.reset();
					updateStartScreen();
					mIsEdited = true;
					mDisplay.setCurrent(mStartScreen);
				} else if (c == cmdNo)
					mDisplay.setCurrent(mStartScreen);
			}

		});

		// Expend amount screen
		mExpendAmountScreen = new TextBox(
				"\u0645\u0642\u062f\u0627\u0631\u0020\u0647\u0632\u064a\u0646\u0647:",
				"", 9, TextField.NUMERIC);
		mExpendAmountScreen.addCommand(cmdBack);
		mExpendAmountScreen.addCommand(cmdOK);
		mExpendAmountScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command cmd, Displayable d) {
				if (cmd == cmdBack)
					mDisplay.setCurrent(mStartScreen);
				if (cmd == cmdOK) {
					mExpendCatScreen.setSelectedIndex(0, true);
					mDisplay.setCurrent(mExpendCatScreen);
				}
			}
		});

		// Expend category screen
		mExpendCatScreen = new List(
				"\u0646\u0648\u0639\u0020\u0647\u0632\u064a\u0646\u0647:",
				List.IMPLICIT);
		final Command cmdSelect = new Command(
				"\u0627\u0646\u062a\u062e\u0627\u0628", Command.ITEM, 0);
		mExpendCatScreen.addCommand(cmdSelect);
		mExpendCatScreen.setSelectCommand(cmdSelect);
		mExpendCatScreen.addCommand(cmdBack);
		mExpendCatScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command cmd, Displayable d) {
				if (cmd == cmdSelect) {
					int amount = Integer.parseInt(mExpendAmountScreen
							.getString());
					int catID = mExpendCatIDs[mExpendCatScreen
							.getSelectedIndex()];
					mTodayBill.addItem(catID, amount);
					mIsEdited = true;
					updateStartScreen();
					mDisplay.setCurrent(mStartScreen);
				}
				if (cmd == cmdBack)
					mDisplay.setCurrent(mExpendAmountScreen);
			}

		});

		// Today's expenditures screen
		mTodayExpendScreen = new Form("\u0647\u0632\u064a\u0646\u0647\u0020\u0647\u0627\u064a\u0020\u0627\u0645\u0631\u0648\u0632:");
		mTodayExpendScreen.addCommand(cmdOK);
		mTodayExpendScreen.setCommandListener(new CommandListener() {
			public void commandAction(Command cmd, Displayable d) {
				if (cmd == cmdOK)
					mDisplay.setCurrent(mStartScreen);
			}
		});
	}

	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {
		shutdown();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		// retrieve expend categories
		try {
			mExpendCats = retrieveExpendCats();
		} catch (RecordStoreFullException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
		}

		// populate expends cats list
		mExpendCatIDs = new int[mExpendCats.size()];
		int i = 0;
		for (Enumeration e = mExpendCats.keys(); e.hasMoreElements();) {
			Integer id = (Integer) e.nextElement();
			mExpendCatIDs[i] = id.intValue();
			mExpendCatScreen.append((String) mExpendCats.get(id), null);
			++i;
		}

		// set up today's bill
		try {
			mTodayBill = Bill.open(TODAY_BILL_RS);
		} catch (RecordStoreNotFoundException e) {
			byte b = 0;
			mTodayBill = new Bill(b, b, b);
		} catch (RecordStoreFullException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
		}

		// set the current display to start screen
		mDisplay = Display.getDisplay(this);
		updateStartScreen();
		mDisplay.setCurrent(mStartScreen);
	}

	private Hashtable retrieveExpendCats() throws RecordStoreFullException,
			RecordStoreException {
		Hashtable catNames = new Hashtable();
		RecordStore catsRS = null;
		// try to open the category names
		try {
			catsRS = RecordStore.openRecordStore(CATS_RS, false);
		} catch (RecordStoreNotFoundException e) {
			// Not opened before, create it
			catsRS = RecordStore.openRecordStore(CATS_RS, true);
			String[] names = makeStaticCatNames();
			// TODO set the encodings in getBytes()
			for (int i = 0; i < names.length; i++)
				try {
					byte[] bytes = names[i].getBytes("UTF-8");
					catsRS.addRecord(bytes, 0, bytes.length);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					error(e1.getMessage());
				}
		}
		RecordEnumeration catsRE = catsRS.enumerateRecords(null, null, false);
		while (catsRE.hasNextElement()) {
			int id = catsRE.nextRecordId();
			byte[] name = catsRS.getRecord(id);
			try {
				catNames.put(new Integer(id), new String(name, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				error(e.getMessage());
			}
		}
		catsRE.destroy();
		catsRS.closeRecordStore();
		return catNames;
	}

	private String[] makeStaticCatNames() {
		return new String[] {
				"\u0645\u062a\u0641\u0631\u0642\u0647", // motefarreghe
				"\u0642\u0633\u0637", // ghest
				"\u0643\u0627\u062f\u0648", // kado
				"\u062e\u0627\u0646\u0647", // khane
				"\u0642\u0628\u0636\u0020\u0645\u0648\u0628\u0627\u064a\u0644",
				"\u0644\u0628\u0627\u0633", // lebas
				"\u0635\u062f\u0642\u0647", // sadaghe
				"\u0643\u0627\u0645\u067e\u064a\u0648\u062a\u0631", // computer
				"\u0627\u064a\u0646\u062a\u0631\u0646\u062a", // internet
				"\u067e\u0632\u0634\u0643", // pezeshk
				"\u062f\u0627\u0631\u0648", // daaroo
				"\u0648\u0631\u0632\u0634\u064a", // varzeshi
				"\u0622\u0631\u0627\u064a\u0634\u064a", // arayeshi
				"\u0628\u0647\u062f\u0627\u0634\u062a\u064a", // behdashti
				"\u0646\u0634\u0631\u064a\u0627\u062a", // nashriat
				"\u062a\u0641\u0631\u064a\u062d\u064a", // tafrihi
				"\u063a\u0630\u0627", // ghaza
				"\u062e\u0648\u0631\u0627\u0643\u064a", // khoraki
				"\u062a\u0627\u0643\u0633\u064a", // taxi
		};
	}

	private void updateStartScreen() {
		((StringItem) mStartScreen.get(0)).setText(mTodayBill.getSum()
				+ " \u062a\u0648\u0645\u0627\u0646");

	}
	
	private void updateTodayExpendsScreen() {
		Enumeration ids = mTodayBill.getItems();
		mTodayExpendScreen.deleteAll();
		boolean isEmpty = true; // if the form is empty
		
		while (ids.hasMoreElements()) {
			isEmpty = false;
			Integer catID = (Integer) ids.nextElement();
			String catName = (String) mExpendCats.get(catID);
			int amount = mTodayBill.getItemCost(catID);
			StringItem si = new StringItem(catName + ":", amount + " \u062a\u0648\u0645\u0627\u0646");
			si.setLayout(Item.LAYOUT_2 | Item.LAYOUT_RIGHT);
			mTodayExpendScreen.append(si);
		}
		if (isEmpty) {
			StringItem nothingYet = new StringItem("", "\u0647\u064a\u0686\u0020\u0647\u0632\u064a\u0646\u0647\u0020\u0627\u064a\u0020\u0645\u0648\u062c\u0648\u062f\u0020\u0646\u064a\u0633\u062a!");
			nothingYet.setLayout(Item.LAYOUT_2 | Item.LAYOUT_RIGHT);
			mTodayExpendScreen.append(nothingYet);
		} else {
			StringItem si = new StringItem("\u062c\u0645\u0639: ", mTodayBill.getSum() + " \u062a\u0648\u0645\u0627\u0646");
			si.setLayout(Item.LAYOUT_2 | Item.LAYOUT_RIGHT);
			mTodayExpendScreen.append(si);
		}
	}

	private void error(String msg) {
		mErrorScreen.setString(msg);
		mDisplay.setCurrent(mErrorScreen);
	}

	private void shutdown() {
		try {
			if (mIsEdited)
				mTodayBill.save(TODAY_BILL_RS);
		} catch (RecordStoreFullException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
		} catch (RecordStoreException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
		}
	}
}
