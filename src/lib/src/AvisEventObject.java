/*
 *  Avis Elvin client library for Java.
 *  
 *  Copyright (C) 2008 Matthew Phillips <avis@mattp.name>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of version 3 of the GNU Lesser General
 *  Public License as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.avis.client;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends java.util.EventObject to add useful utilities such as
 * general data association.
 * 
 * @author Matthew Phillips
 */
public abstract class AvisEventObject extends EventObject
{
  private Map<String, Object> data;

  /**
   * Create a new instance.
   * 
   * @param source
   */
  public AvisEventObject (Object source)
  {
    this (source, new HashMap<String, Object> ());
  }
  
  /**
   * Create a new instance.
   * 
   * @param source The event source.
   * @param data The initial associated data.
   */
  public AvisEventObject (Object source, Map<String, Object> data)
  {
    super (source);
    
    this.data = data;
  }

  /**
   * Set some generic data associated with the event.
   * 
   * @see #getData(String)
   */
  public void setData (String key, Object value)
  {
    data.put (key, value);
  }
  
  /**
   * Get some data previously associated with the event.
   * 
   * @return The data, or null if none set for the key.
   * 
   * @see #setData(String, Object)
   */
  public Object getData (String key)
  {
    return data.get (key);
  }
}
