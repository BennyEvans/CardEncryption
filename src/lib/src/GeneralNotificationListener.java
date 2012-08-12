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
 * A listener to notifications received by any subscription created by
 * an {@linkplain Elvin elvin connection}. This differs from
 * {@linkplain NotificationListener notification listeners added to a
 * subscription} in that all notifications received by a connection
 * are available to this type of listener.
 * <p>
 * A general subscription listener can be useful in the case where
 * multiple subscriptions match a notification and the client only
 * wishes to process each notification once. Another way to handle
 * this would be to mark processed notifications with the
 * {@link AvisEventObject#setData(String, Object)} and
 * {@link AvisEventObject#getData(String)} methods.
 * 
 * @see Elvin#addNotificationListener(GeneralNotificationListener)
 * @see NotificationListener
 * 
 * @author Matthew Phillips
 */
public interface GeneralNotificationListener extends EventListener
{
  /**
   * Called when a notification is received.
   */
  public void notificationReceived (GeneralNotificationEvent e);
}
