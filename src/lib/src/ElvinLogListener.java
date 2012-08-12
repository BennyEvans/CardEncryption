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

import java.util.EventListener;

/**
 * A listener for messages logged by an elvin client.
 * 
 * @see Elvin#addLogListener(ElvinLogListener)
 * @see ElvinLogEvent
 * 
 * @author Matthew Phillips
 */
public interface ElvinLogListener extends EventListener
{
  /**
   * Called when a message is logged by the elvin client.
   */
  public void messageLogged (ElvinLogEvent e);
}
