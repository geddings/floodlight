package net.floodlightcontroller.debugevent;

import java.util.ArrayList;
import java.util.List;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugevent.DebugEvent.EventInfo;

public interface IDebugEventService extends IFloodlightService {

    /**
     * Different event types. Events that are meant to be logged on demand
     * need to be separately enabled/disabled.
     */
    public enum EventType {
        ALWAYS_LOG,
        LOG_ON_DEMAND
    }

    /**
     *  A limit on the maximum number of event types that can be created
     */
    public static final int MAX_EVENTS = 2000;

    /**
     * Public class for information returned in response to rest API calls.
     */
    public class DebugEventInfo {
        EventInfo eventInfo;
        ArrayList<String> events;

        public DebugEventInfo(EventInfo eventInfo, ArrayList<String> eventHistory) {
            this.eventInfo = eventInfo;
            this.events = eventHistory;
        }

        public EventInfo getEventInfo() {
            return eventInfo;
        }

        public ArrayList<String> getEvents() {
            return events;
        }
    }

    /**
    * exception thrown when MAX_EVENTS have been registered
    */
    public class MaxEventsRegistered extends Exception {
        private static final long serialVersionUID = 2609587082227510262L;
    }

    /**
     * Register an event for debugging.
     *
     * @param moduleName       module registering event eg. linkdiscovery, virtualrouting.
     * @param eventName        name given to event.
     * @param flushNow         set true for rare events that are not triggered
     *                         in the packet processing pipeline (eg. switch
     *                         connect/disconnect).
     * @param eventDescription A descriptive string describing event.
     * @param eventType        EventType for this event.
     * @param bufferCapacity   Number of events to store for this event in a circular
     *                         buffer. Older events will be discarded once the
     *                         buffer is full.
     * @param formatStr        A descriptive string for displaying the 'params'
     *                         For example, if 'params' consists of 3 objects -
     *                         a dpid, an IP address, and a number, the format string
     *                         can be "dpid=%dpid, ipAddr=%ipv4, count=%d".
     *                         Any normal conversion can be used including %d, %x, %s etc.
     *                         In addition 3 special conversions can be used
     *                         %dpid which displays xx:xx:xx:xx:xx:xx:xx:xx
     *                         %mac  which displays xx:xx:xx:xx:xx:xx
     *                         %ipv4 which displays ip addrs in dotted decimal form
     * @param params           an Object[] with the parameters to register for this
     *                         event. This can just be null for now.
     * @return                 an eventId for this event. All updates to this
     *                         event must use the returned eventId.
     * @throws MaxEventsRegistered
     */
    public int registerEvent(String moduleName, String eventName, boolean flushNow,
                             String eventDescription, EventType eventType,
                             int bufferCapacity, String formatStr, Object[] params)
                                     throws MaxEventsRegistered;

    /**
     * updateEvent is used to log events for pre-registered events.  This method
     * will not check to see if the parameters passed in are consistent on
     * each invocation for the same event.
     *
     * @param eventId     The id of the pre-registered event
     * @param params      an Object[] with the parameters to log for this event.
     *                    For example, switch dpids, host macs or ip-addrs or
     *                    any other user defined parameter. Once a set of params
     *                    are used with an event, the same event should always
     *                    be updated with the same type of parameters in the same order.
     *                    i.e different parts of the code base can update the same
     *                    event but it should do so in the same way, maintaining
     *                    the order of parameters.
     *                    updateEvent(3, new Object[] { dpid1, ip1, "connected" })
     *                    updateEvent(3, new Object[] { dpid4, ip4, "disconnected" })
     */
    public void updateEvent(int eventId, Object[] params);

    /**
     * Update the global event stores with values from the thread local stores. This
     * method is not typically intended for use by any module. It's typical usage is from
     * floodlight core for events that happen in the packet processing pipeline.
     * For other rare events, flushEvents should be called.
     */
    public void flushEvents();

    /**
     * Determine if eventName is a registered event for a given moduleName
     */
    public boolean containsModuleEventName(String moduleName, String eventName);

    /**
     * Determine if any events have been registered for module of name moduleName
     */
    public boolean containsModuleName(String moduleName);

    /**
     * Get event history for all events. This call can be expensive as it
     * formats the event histories for all events.
     *
     * @return  a list of all event histories or an empty list if no events have
     *          been registered
     */
    public List<DebugEventInfo> getAllEventHistory();

    /**
     * Get event history for all events registered for a given moduleName
     *
     * @return  a list of all event histories for all events registered for the
     *          the module or null if there are no events for this module
     */
    public List<DebugEventInfo> getModuleEventHistory(String moduleName);

    /**
     * Get event history for a single event
     *
     * @param  moduleName  registered module name
     * @param  eventName   registered event name for moduleName
     * @return DebugEventInfo for that event, or null if the moduleEventName
     *         does not correspond to a registered event.
     */
    public DebugEventInfo getSingleEventHistory(String moduleName, String eventName);

    /**
     * Wipe out all event history for all registered active events
     */
    public void resetAllEvents();

    /**
     * Wipe out all event history for all events registered for a specific module
     *
     * @param moduleName  registered module name
     */
    public void resetAllModuleEvents(String moduleName);

    /**
     * Wipe out event history for a single event
     * @param  moduleName  registered module name
     * @param  eventName   registered event name for moduleName
     */
    public void resetSingleEvent(String moduleName, String eventName);

    /**
     * Retrieve information on all registered events
     *
     * @return the arraylist of event-info or an empty list if no events are registered
     */
    public ArrayList<EventInfo> getEventList();


}
