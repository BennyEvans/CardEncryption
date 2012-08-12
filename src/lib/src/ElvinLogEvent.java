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

/**
 * A logging event fired by an elvin client.
 * 
 * @see Elvin#addLogListener(ElvinLogListener)
 * 
 * @author Matthew Phillips
 */
public class ElvinLogEvent extends EventObject
{
  public enum Type {DIAGNOSTIC, WARNING, ERROR}
  
  /**
   * The type of message.
   */
  public final Type type;
  
  /**
   * The message text.
   */
  public final String message;
  
  /**
   * The exception that triggered the message, if any (or null).
   */
  public final Throwable exception;

  public ElvinLogEvent (Elvin elvin, Type type, 
                        String message, Throwable exception)
  {
    super (elvin);
    
    this.type = type;
    this.message = message;
    this.exception = exception;
  }
  
  /**
   * The elvin client connection that fired the event. Same as
   * {@link #getSource()}.
   */
  public Elvin elvin ()
  {
    return (Elvin)getSource ();
  }
}
