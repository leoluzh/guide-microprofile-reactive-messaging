package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

import io.openliberty.guides.models.SystemLoad;
import io.reactivex.rxjava3.core.Flowable;

@ApplicationScoped
public class SystemService {

	private static final OperatingSystemMXBean OS_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();
	private static String HOSTNAME = null;
	
	private static String getHostname() {
		if( Objects.isNull( HOSTNAME ) ) {
			try {
				return InetAddress.getLocalHost().getHostName();
			}catch( UnknownHostException ex ) {
				return System.getenv("HOSTNAME");
			}
		}
		return HOSTNAME;
	}
	
	//publishes to systemLoadTopic via systemLoad channel.
	@Outgoing("systemLoad")
	public Publisher<SystemLoad> sendSystemLoad(){
		return Flowable.interval( 15, TimeUnit.SECONDS )
				.map( interval -> new SystemLoad( 
						getHostname() , 
						new Double( OS_MX_BEAN.getSystemLoadAverage() )));
	}
	
}
