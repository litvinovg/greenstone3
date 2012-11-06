/*
 *    SimpleMacroResolver.java
 *    Copyright (C) 2012 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;


import org.apache.commons.lang3.*;
import org.greenstone.gsdl3.util.MacroResolver.Macro;

public class SimpleMacroResolver extends MacroResolver
{

  public SimpleMacroResolver()
  { 
    super();
  }

  public void addMacro(String macro, String text) {
    Macro m = new Macro();
    m.type = TYPE_TEXT;
    m.macro = macro;
    m.text = text;
    m.resolve = false;
    addMacro(m, SCOPE_TEXT);
  }

  // this ignores all other considerations, is a straight text swap
  public String resolve(String text)
  {
    for (int i=0; i<text_macros.size(); i++) {
      Macro m = text_macros.get(i);
      text = StringUtils.replace(text, m.macro, m.text);
    }
    return text;
  }
  public String resolve(String text, String lang, String scope, String doc_oid)
  {
    return resolve(text);
  }


}
