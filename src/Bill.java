import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class Bill {
	
	private Hashtable items; // maps catIDs to costs

	private int sum = 0;

	private byte year, month, day;

	public Bill(byte year, byte month, byte day) {
		this.year = year;
		this.month = month;
		this.day = day;
		items = new Hashtable();
	}

	public void addItem(int catID, int amount) {
		Integer id = new Integer(catID);
		if (items.containsKey(id)) {
			int prev = ((Integer) items.get(id)).intValue();
			items.put(id, new Integer(amount + prev));
		} else
			items.put(id, new Integer(amount));
		sum += amount;
	}
	
	public Enumeration getItems() {
		return items.keys();
	}
	
	public int getItemCost(Integer item) {
		return ((Integer)items.get(item)).intValue();
	}
	
	public int getSum() {
		return sum;
	}

	public void save(String rsName) throws RecordStoreFullException, RecordStoreException {
		// create the record. Record format:
		// year|month|day|items count|    cat    |   amount   | ... |    cat   |    amount   |
		byte[] record = new byte[7 + 8 * items.size()];
		record[0] = year;
		record[1] = month;
		record[2] = day;
		Convert.int2bytes(items.size(), record, 3);
		int i = 0;
		for (Enumeration e = items.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			int catID = ((Integer)key).intValue();
			int amount = ((Integer)items.get(key)).intValue();
			Convert.int2bytes(catID, record, 7 + 8 * i);
			Convert.int2bytes(amount, record, 11 + 8 * i);
			i++;
		}
		// save it in the recordset
		RecordStore rs = RecordStore.openRecordStore(rsName, true);
		RecordEnumeration re = rs.enumerateRecords(null, null, false);
		if (re.hasNextElement()) {
			int recID = re.nextRecordId();
			rs.setRecord(recID, record, 0, record.length);
		} else {
			rs.addRecord(record, 0, record.length);
		}
		re.destroy();
		rs.closeRecordStore();
	}
	
	public void reset() {
		items.clear();
		sum = 0;
	}
	
	public static Bill open(String rsName) throws RecordStoreFullException,
			RecordStoreNotFoundException, RecordStoreException {
		// retrieve data
		RecordStore rs = RecordStore.openRecordStore(rsName, false);
		RecordEnumeration re = rs.enumerateRecords(null, null, false);
		byte[] record = re.nextRecord();
		re.destroy();
		rs.closeRecordStore();

		// make the bill
		Bill bill = new Bill(record[0], record[1], record[2]);
		int numItems = Convert.bytes2int(record, 3);
		bill.sum = 0;
		for (int i = 0; i < numItems; i++) {
			Integer id = new Integer(Convert.bytes2int(record, 7 + 8 * i));
			Integer amount = new Integer(Convert.bytes2int(record, 11 + 8 * i));
			bill.items.put(id, amount);
			bill.sum += amount.intValue();
		}
		return bill;
	}
}
