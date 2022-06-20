package net.pennix.maven.wagon;

import static java.lang.System.getenv;

import java.io.IOException;

import org.apache.maven.wagon.WagonTestCase;
import org.junit.Test;

public class S3v2WagonTest extends WagonTestCase {

	@Test
	public void test(
	) throws Throwable {
		testWagon();
		//fail("Not yet implemented");
	}

	@Override
	protected String getTestRepositoryUrl(
	) throws IOException {
		return getenv("TEST_REPO_URL");
	}

	@Override
	protected String getProtocol(
	) {
		return "s3";
	}
}
