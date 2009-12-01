/*
 * (C) Pharos IP Pty Ltd 1997
 */

package au.com.pharos.dbinterface.vf;

import java.util.Date;

import au.com.pharos.gdbm.GdbmFile;

import au.com.pharos.packing;

import au.com.pharos.dbinterface.vf;

public class VFDbInterface {

  /* The Order database */
  private String orderDB = "/fn/data/Orders.gdbm";

  /* The Stock database */
  private String stockDB = "/fn/data/Stock.gdbm";

  /* Initialisation.  Nothing needs to happen yet. */
  public VFDbInterface()
  {
    ;
  }

  /* Submit an order to the database */
  public void submitOrder(VFOrder order)
  {
    GdbmFile db = new GdbmFile(orderDB);
    db.setKeyPacking(new StringPacking());
    db.setValuePacking(new QuotedVectorPacking());

    Vector details = order.toVectorString();
    
