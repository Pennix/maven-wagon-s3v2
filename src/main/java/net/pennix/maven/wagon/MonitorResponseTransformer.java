package net.pennix.maven.wagon;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferEventSupport;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class MonitorResponseTransformer implements ResponseTransformer<GetObjectResponse, GetObjectResponse> {

	private File file;

	private TransferEventSupport support;

	private TransferEvent event;

	public MonitorResponseTransformer(
			File file,
			TransferEventSupport support,
			TransferEvent event

	) {
		this.file = file;
		this.support = support;
		this.event = event;

		if (this.file.exists())
			this.file.delete();
	}

	@Override
	public GetObjectResponse transform(
			GetObjectResponse response,
			AbortableInputStream inputStream
	) throws Exception {
		try (InputStream in = inputStream; OutputStream out = new FileOutputStream(file)) {
			byte[] buffer = new byte[4096];
			for (int c = in.read(buffer); c > 0; c = in.read(buffer)) {
				TransferEvent event = new TransferEvent(this.event.getWagon(), this.event.getResource(), this.event.getEventType(), this.event.getRequestType());
				event.setTimestamp(currentTimeMillis());
				support.fireTransferProgress(event, buffer, c);

				out.write(buffer, 0, c);
			}
		}
		return response;
	}
}
