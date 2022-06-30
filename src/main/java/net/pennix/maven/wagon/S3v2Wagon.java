package net.pennix.maven.wagon;

import static org.apache.maven.wagon.events.TransferEvent.REQUEST_GET;
import static org.apache.maven.wagon.events.TransferEvent.REQUEST_PUT;
import static org.apache.maven.wagon.events.TransferEvent.TRANSFER_PROGRESS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3v2Wagon extends AbstractWagon {

	//private S3AsyncClient client;
	private S3Client client;

	private String bucket;

	private String baseDir;

	@Override
	protected void openConnectionInternal(
	) throws ConnectionException, AuthenticationException {
		//this.client = S3AsyncClient.create();
		this.client = S3Client.create();

		Repository repo = getRepository();
		this.bucket = repo.getHost();
		this.baseDir = repo.getBasedir();
	}

	@Override
	public void get(
			String resourceName,
			File destination
	) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		Path key = Paths.get(baseDir, resourceName);
		key = Paths.get("/").relativize(key);

		GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key.toString()).build();

		Resource resource = new Resource(resourceName);
		fireGetInitiated(resource, destination);

		try {
			TransferEvent event = new TransferEvent(this, resource, TRANSFER_PROGRESS, REQUEST_GET);
			fireGetStarted(resource, destination);
			this.client.getObject(request, new MonitorResponseTransformer(destination, getTransferEventSupport(), event));
		} catch (NoSuchKeyException e) {
			throw new ResourceDoesNotExistException(e.toString(), e);
		}
		fireGetCompleted(resource, destination);
	}

	@Override
	public boolean getIfNewer(
			String resourceName,
			File destination,
			long timestamp
	) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		Path key = Paths.get(baseDir, resourceName);
		key = Paths.get("/").relativize(key);

		HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucket).key(key.toString()).build();
		try {
			HeadObjectResponse response = this.client.headObject(request);
			Instant lastModified = response.lastModified();
			if (lastModified.toEpochMilli() > timestamp) {
				this.get(resourceName, destination);
				return true;
			} else
				return false;
		} catch (NoSuchKeyException e) {
			throw new ResourceDoesNotExistException(e.toString(), e);
		}
	}

	@Override
	public void put(
			File source,
			String destination
	) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		Path key = Paths.get(baseDir, destination);
		key = Paths.get("/").relativize(key);

		PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key.toString()).build();

		Resource resource = new Resource(destination);
		firePutInitiated(resource, source);

		firePutStarted(resource, source);
		TransferEvent event = new TransferEvent(this, resource, TRANSFER_PROGRESS, REQUEST_PUT);
		try (InputStream in = new MonitorInputStream(source, getTransferEventSupport(), event)) {
			this.client.putObject(request, RequestBody.fromInputStream(in, source.length()));
		} catch (FileNotFoundException e) {
			throw new ResourceDoesNotExistException(e.toString(), e);
		} catch (IOException e) {
			throw new TransferFailedException(e.toString(), e);
		}
		firePutCompleted(resource, source);
	}

	@Override
	public void closeConnection(
	) {
		if (client != null)
			client.close();
	}
}
