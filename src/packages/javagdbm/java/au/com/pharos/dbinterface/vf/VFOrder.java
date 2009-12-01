/*
 * (C) Pharos IP PTy Ltd 1997
 */

package au.com.pharos.dbinterface.vf;

import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import java.text.SimpleDateFormat;

public class VFOrder {

  /* The final date for submission */
  public Date finalSubmit;

  /* The date the order was created */
  public Date orderDate;

  /* A hash mapping titles requested to numbers of each title*/
  public Hashtable orderNumbers;
  
  /* The name of the ordering store */
  public String name;

  public VFOrder(Date newFinal,
		 String newName,
		 Hashtable newOrder)
  {
    finalSubmit = newFinal;
    orderNumbers = newOrder;
    name = newName;
    orderDate = new Date(System.currentTimeMillis());
    if (orderDate.after(finalSubmit)) {
      throw new IllegalArgumentException("Final submission date is earlier than present date.");
    }
  }

  public VFOrder(Date newFinal,
		 Date newDate,
		 String newName,
		 Hashtable newOrder)
  {
    finalSubmit = newFinal;
    orderDate = newDate;
    orderNumbers = newOrder;
    name = newName;
    orderDate = new Date(System.currentTimeMillis());
    if (orderDate.after(finalSubmit)) {
      throw new IllegalArgumentException("Final submission date is earlier than present date.");
    }
  }

  public Vector toVectorString()
  {
    Vector result = new Vector();
    result.addElement(new Long(finalSubmit.getTime()));
    result.addElement(new Long(orderDate.getTime()));
    result.addElement(orderNumbers.toString());
    result.addElement(name);
    return result;
  }

  public VFOrder fromVectorString(Vector input)
       throws java.text.ParseException
  {
    Hashtable data = new Hashtable();
    SimpleDateFormat form = new SimpleDateFormat();
    for (Enumeration e = input.elements(); e.hasMoreElements(); ) {
      System.out.println(e.nextElement());
    }
    try {
      finalSubmit = new Date(((Long) input.elementAt(0)).longValue());
    } catch (NullPointerException e) {
      System.err.println("Waaaah!");
      finalSubmit = new Date();
    }
    System.err.println("No 2:" + input.elementAt(1).toString());
    orderDate = new Date(((Long) input.elementAt(1)).longValue());
    name = (String) input.elementAt(3);
    String hashString = (String) input.elementAt(2);
    StringBuffer key = new StringBuffer();;
    StringBuffer value = new StringBuffer();
    String type = new String("key");
    for (int count = 0; count < hashString.length(); count++) {
      char i = hashString.charAt(count);
      if (i == '{' || i == ' ') { ; }
      else if (i == '}') { data.put(key, value); }
      else if (i == ',') {data.put(key, value); }
      else if (i == '=') {
	if (type.equals("key")) {
	  type = "value";
	} else {
	  type = "key";
	}
      } else {
	if (type.equals("key")) {
	  key.append(i);
	} else {
	  value.append(i);
	}
      }
    }
    return new VFOrder(finalSubmit, orderDate, name, data);
  }
	
      


}
  
