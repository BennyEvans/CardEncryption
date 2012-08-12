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

import java.io.IOException;

import org.avis.io.messages.Nack;
import org.avis.io.messages.XidMessage;

/**
 * An exception indicating the Elvin router rejected (NACK'd) one of
 * the client's requests.
 * 
 * @author Matthew Phillips
 */
public class RouterNackException extends IOException
{
  RouterNackException (String message)
  {
    super (message);
  }
  
  RouterNackException (XidMessage request, Nack nack)
  {
    super ("Router rejected " + request.name () +
           ": " + nack.errorCodeText () +
           ": " + nack.formattedMessage ());
  }
}
