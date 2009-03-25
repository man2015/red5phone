package org.red5.server.webapp.sip;


import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IPlayItem;
import org.red5.server.api.stream.IPlaylistSubscriberStream;
import org.red5.server.api.stream.IStreamAwareScopeHandler;
import org.red5.server.api.stream.ISubscriberStream;


public class Application extends ApplicationAdapter implements IStreamAwareScopeHandler {

    protected static Logger log = LoggerFactory.getLogger( Application.class );

    private SIPManager sipManager;

    private boolean available = false;

    private int startSIPPort = 5070;

    private int stopSIPPort = 5099;

    private int sipPort;

    private int startRTPPort = 3000;

    private int stopRTPPort = 3029;

    private int rtpPort;

    private Map< String, String > userNames = new HashMap< String, String >();


    @Override
    public boolean appStart( IScope scope ) {

        loginfo( "Red5SIP starting in scope " + scope.getName() + " " + System.getProperty( "user.dir" ) );
        sipManager = SIPManager.getInstance();

        // startSIPPort =
        // Integer.parseInt(PacketHandler.getInstance().getStartSIPPort());
        // stopSIPPort =
        // Integer.parseInt(PacketHandler.getInstance().getEndSIPPort());
        // startRTPPort =
        // Integer.parseInt(PacketHandler.getInstance().getStartRTPPort());
        // stopRTPPort =
        // Integer.parseInt(PacketHandler.getInstance().getEndRTPPort());

        sipPort = startSIPPort;
        rtpPort = startRTPPort;
        return true;
    }


    @Override
    public void appStop( IScope scope ) {

        loginfo( "Red5SIP stopping in scope " + scope.getName() );
        sipManager.destroyAllSessions();
    }


    @Override
    public boolean appConnect( IConnection conn, Object[] params ) {

        IServiceCapableConnection service = (IServiceCapableConnection) conn;
        loginfo( "Red5SIP Client connected " + conn.getClient().getId() + " service " + service );
        return true;
    }


    @Override
    public boolean appJoin( IClient client, IScope scope ) {

        loginfo( "Red5SIP Client joined app " + client.getId() );
        IConnection conn = Red5.getConnectionLocal();
        IServiceCapableConnection service = (IServiceCapableConnection) conn;

        return true;
    }


    @Override
    public void appLeave( IClient client, IScope scope ) {

        IConnection conn = Red5.getConnectionLocal();
        loginfo( "Red5SIP Client leaving app " + client.getId() );

        if ( userNames.containsKey( client.getId() ) ) {
            loginfo( "Red5SIP Client closing client " + userNames.get( client.getId() ) );
            sipManager.closeSIPUser( userNames.get( client.getId() ) );
            userNames.remove( client.getId() );
        }
    }


    public void streamPublishStart( IBroadcastStream stream ) {

        loginfo( "Red5SIP Stream publish start: " + stream.getPublishedName() );
        IConnection current = Red5.getConnectionLocal();

    }


    public void streamBroadcastClose( IBroadcastStream stream ) {

        loginfo( "Red5SIP Stream broadcast close: " + stream.getPublishedName() );
    }


    public void streamBroadcastStart( IBroadcastStream stream ) {

        loginfo( "Red5SIP Stream broadcast start: " + stream.getPublishedName() );

    }


    public void streamPlaylistItemPlay( IPlaylistSubscriberStream stream, IPlayItem item, boolean isLive ) {

        loginfo( "Red5SIP Stream play: " + item.getName() );
    }


    public void streamPlaylistItemStop( IPlaylistSubscriberStream stream, IPlayItem item ) {

        loginfo( "Red5SIP Stream stop: " + item.getName() );
    }


    public void streamPlaylistVODItemPause( IPlaylistSubscriberStream stream, IPlayItem item, int position ) {

    }


    public void streamPlaylistVODItemResume( IPlaylistSubscriberStream stream, IPlayItem item, int position ) {

    }


    public void streamPlaylistVODItemSeek( IPlaylistSubscriberStream stream, IPlayItem item, int position ) {

    }


    public void streamSubscriberClose( ISubscriberStream stream ) {

        loginfo( "Red5SIP Stream subscribe close: " + stream.getName() );

    }


    public void streamSubscriberStart( ISubscriberStream stream ) {

        loginfo( "Red5SIP Stream subscribe start: " + stream.getName() );

    }


    public List< String > getStreams() {

        IConnection conn = Red5.getConnectionLocal();
        return getBroadcastStreamNames( conn.getScope() );
    }


    public void onPing() {

        loginfo( "Red5SIP Ping" );
    }


	public void open(String uid, String phone,String username, String password, String realm, String proxy) {
		loginfo("Red5SIP open");

		login(uid, phone, username, password, realm, proxy);
		register(uid);
	}


	public void login(String uid, String phone, String username, String password, String realm, String proxy) {
		loginfo("Red5SIP login " + uid);

		IConnection conn = Red5.getConnectionLocal();
		IServiceCapableConnection service = (IServiceCapableConnection) conn;

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser == null) {
			loginfo("Red5SIP open creating sipUser for " + username + " on sip port " + sipPort + " audio port " + rtpPort + " uid " + uid );

			try {
				sipUser = new SIPUser(conn.getClient().getId(), service, sipPort, rtpPort);
				sipManager.addSIPUser(uid, sipUser);

			} catch (Exception e) {
				loginfo("open error " + e);
			}
		}

		sipUser.login(phone,username, password, realm, proxy);
		userNames.put(conn.getClient().getId(), uid);

		sipPort++;
		if (sipPort > stopSIPPort) sipPort = startSIPPort;

		rtpPort++;
		if (rtpPort > stopRTPPort) rtpPort = startRTPPort;

	}



	public void register(String uid) {
		loginfo("Red5SIP register");

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser != null) {
			sipUser.register();
		}
	}


	public void call(String uid, String destination) {
		loginfo("Red5SIP Call " + destination);

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser != null) {
			loginfo("Red5SIP Call found user " + uid + " making call to " + destination);
			sipUser.call(destination);
		}

	}

	public void dtmf(String uid, String digits) {
		loginfo("Red5SIP DTMF " + digits);

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser != null) {
			loginfo("Red5SIP DTMF found user " + uid + " sending dtmf digits " + digits);
			sipUser.dtmf(digits);
		}

	}

	public void accept(String uid) {
		loginfo("Red5SIP Accept");

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser != null) {
			sipUser.accept();
		}
	}
	//Lior Add

	public void unregister(String uid) {
			loginfo("Red5SIP unregister");

			SIPUser sipUser = sipManager.getSIPUser(uid);

			if(sipUser != null) {
				sipUser.unregister();
			}
	}

	public void hangup(String uid) {
		loginfo("Red5SIP Hangup");

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser != null) {
			sipUser.hangup();
		}
	}

	public void streamStatus(String uid, String status) {
		loginfo("Red5SIP streamStatus");

		SIPUser sipUser = sipManager.getSIPUser(uid);

		if(sipUser != null) {
			sipUser.streamStatus(status);
		}
	}


	public void close(String uid) {
		loginfo("Red5SIP endRegister");

		IConnection conn = Red5.getConnectionLocal();
		sipManager.closeSIPUser(uid);
		userNames.remove(conn.getClient().getId());
	}



    private void loginfo( String s ) {

        log.info( s );
    }
}
