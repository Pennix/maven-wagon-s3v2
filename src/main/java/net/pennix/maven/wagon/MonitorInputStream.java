package net.pennix.maven.wagon;

import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferEventSupport;

public class MonitorInputStream extends InputStream {

	private InputStream wrapped;

	private TransferEventSupport support;

	private TransferEvent event;

	public MonitorInputStream(
			File file,
			TransferEventSupport support,
			TransferEvent event
	) throws FileNotFoundException {
		super();
		this.wrapped = new FileInputStream(file);
		this.support = support;
		this.event = event;
	}

	@Override
	public int read(
	) throws IOException {
		return wrapped.read();
	}

	@Override
	public int read(
			byte[] b,
			int off,
			int len
	) throws IOException {
		int c = super.read(b, off, len);

		TransferEvent event = new TransferEvent(this.event.getWagon(), this.event.getResource(), this.event.getEventType(), this.event.getRequestType());
		event.setTimestamp(currentTimeMillis());

		byte[] mb = b;
		if (c != b.length) {
			mb = new byte[c];
			arraycopy(b, 0, mb, 0, c);
		}
		support.fireTransferProgress(event, mb, c);

		return c;
	}
}
