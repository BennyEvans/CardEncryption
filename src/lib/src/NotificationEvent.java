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

/**
 * A notification event sent to subscription listeners.
 * 
 * @see Subscription#addListener(NotificationListener)
 * 
 * @author Matthew Phillips
 */
public final class NotificationEvent extends AvisEventObject
{
  /**
   * The subscription that matched the notification. This is the same
   * as {@link #getSource()}.
   */
  public final Subscription subscription;
  
  /**
   * The notification received from the router.
   */
  public final Notification notification;
  
  /**
   * True if the notification was received securely from a client with
   * compatible security keys.
   */
  public final boolean secure;

  NotificationEvent (Subscription subscription,
                     Notification notification,
                     boolean secure,
                     Map<String, Object> data)
  {
    super (subscription, data);
    
    this.subscription = subscription;
    this.notification = notification;
    this.secure = secure;
  }
}
