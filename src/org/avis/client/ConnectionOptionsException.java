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

import java.util.Map;

import java.io.IOException;

import static org.avis.util.Text.mapToString;

/**
 * Thrown when the Elvin client receives a rejection of one or more
 * requested connection options.
 * 
 * @author Matthew Phillips
 */
public class ConnectionOptionsException extends IOException
{
  /** The requested options */
  public final ConnectionOptions options;
  /** The rejected options and the actual value that the server will use. */
  public final Map<String, Object> rejectedOptions;

  ConnectionOptionsException (ConnectionOptions options,
                                     Map<String, Object> rejectedOptions)
  {
    super ("Router rejected connection options: " +
            "rejected options and actual values: " +
            mapToString (rejectedOptions));
    
    this.options = options;
    this.rejectedOptions = rejectedOptions;
  }
}
