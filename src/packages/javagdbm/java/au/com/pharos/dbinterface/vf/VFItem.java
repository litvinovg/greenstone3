/*
 * (C) Pharos IP Pty Ltd 1997
 */
package au.com.pharos.dbinterface.vf;

import java.util.Date;

public class VFItem {

  /* The title of the item */
  public String title;

  /* The name fo the item's distributor */
  public String distributor;

  /* The category of the item */
  // TODO: Create a more appropriate type for this?
  public String category;

  /* The rating of the item */
  //TODO: Again, a more appropriate type?
  public String rating;

  /* The list price */
  public float listPrice;

  /* The supplier's price */
  public float ourPrice;

  /* The release date for the item */
  public Date releaseDate;

  /* The date upon which the video is accessible to PayTV */
  public Date payTVWindow;

  /* The date upon which the video is accessible to free-to-air TV */
  public Date tvWindow;

  /* The sell-through beginning date */
  public Date sellThruWindow;

  /* Whether or not the item has an internal magazine advertisement */
  public boolean magAdvert;

  /* The recommended number of items for Budget 1 stores */
  public short budget1;

  /* For budget 2 stores */
  public short budget2;

  /* Budget 3 stores */
  public short budget3;

  /* Budget 4 stores */
  public short budget4;

  /* Will the item be advertised by the VideoIndustry Professional Association */
  boolean vipa;

  /* Initialise all the attributes */
  public VFItem(String newTitle,
		String newDistrib,
		String newCat,
		String newRating,
		float newList,
		float newPrice,
		Date newRelease,
		Date newPaytv,
		Date newTv,
		boolean isMagAdvert,
		short newBudget1,
		short newBudget2,
		short newBudget3,
		short newBudget4
		boolean newvipa)
  {
    title = newTitle;
    distributor = newDistrib;
    category = newCat;
    rating = newRating;
    listPrice = newList;
    ourPrice = newPrice;
    releaseDate = newRelease;
    payTVWindow = newPaytv;
    tvWindow = newTv;
    magAdvert = isMagAdvert;
    budget1 = newBudget1;
    budget2 = newBudget2;
    budget3 = newBudget3;
    budget4 = newBudget4;
    vipa = newvipa;
  }

  public boolean equals (Object o)
  {
    if (!(o instanceof VFItem)) {
      return false;
    }
    VFItem other = (VFItem) o;
    if (title.equals(other.title) && distributor.equals(other.distributor) && category.equals(other.category)) {
      return true;
    }
    return false;
  }

}
