package net.floodlightcontroller.debugevent.web;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class DebugEventRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/{param}", DebugEventResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/debugevent";
    }

}
