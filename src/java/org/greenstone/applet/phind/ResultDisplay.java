/**********************************************************************
 *
 * ResultDisplay.java -- Phrase list holder for Phind java applet
 *
 * Copyright 1997-2000 Gordon W. Paynter
 * Copyright 2000 The New Zealand Digital Library Project
 *
 * A component of the Greenstone digital library software
 * from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *********************************************************************/

/*********************************************************************

This class is used in the Phind java applet (Phind.java).

A ResultDisplay is a Panel that can display a single Component, which will
be a ResultBox or PhindTitle object.

ResultBoxes can be "chained" together with their "prev" and "next" fields,
allowing us to have an arbitrary number of "displays" in which to show
phrase lists.

Main methods:
* emptyContents will remove the component and emptyAll the "next" display
* display will add an item to the first (ie: most "prev") available box

**********************************************************************/
package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

import java.awt.Panel;
import java.awt.BorderLayout;
import java.awt.Component;

class ResultDisplay extends Panel {

  Phind phind;
  ResultDisplay prev, next;
  Component current;
  boolean empty;

  // Create a new Display
  ResultDisplay(Phind ph, ResultDisplay pr) {
    phind = ph;
    prev = pr;
    next = null;
    if (prev != null) prev.next = this;
    setLayout(new BorderLayout());

    empty = true;
    current = new PhindTitle(ph);
    add(current);
  }

  // Empty the contents of the display,
  // and all the next displays
  void emptyContents() {
    if (empty == false) {
      empty = true;
      current = new PhindTitle(phind);
      removeAll();
      add(current);
    }
    if (next != null) next.emptyContents();
  }

  // Set the contents of this display
  Component setContents( Component r ) {
    removeAll();
    add("Center", r);
    current = r;
    empty = false;
    if (r.getClass().getName().equals("ResultBox")) {
      ((ResultBox) r).display = this;
    }
    return r;
  }


  
  // Display a component in the "most previous" display
  ResultDisplay display( Component r ) {

    if (prev == null) {
      // if there is no previous box, add the component here
      setContents(r);
      return this;

    } else if (prev.empty == true) {
      // if the previous box is empty, add the component to it instead
      return prev.display(r);

    } else if (empty == true) {
      // if this box is empty, add the compnent to it
      setContents(r);
      return this;

    } else {
      // if this box is occupied,
      // shift it's contents prev-wards and add here.
      prev.display(current);
      setContents(r);
      return this;
    }
  }

}



