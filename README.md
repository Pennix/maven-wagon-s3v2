Maven wagon for AWS S3 using awssdk v2
=========================================

# Background

This package provides a maven wagon provider that connects to aws s3.

There's another great one by [seahen](https://github.com/seahen/maven-s3-wagon), but it does not support aws-cn regions.

So I made this to fully utilize awssdk capabilities -- any config method supported by awssdk.

# Maven Configuration

``` xml
<build>
	<extensions>
		<extension>
			<groupId>net.pennix</groupId>
			<artifactId>maven-wagon-s3v2</artifactId>
			<!-- NOTE: change this to the most recent release version from the repo -->
			<version>1.0.0</version>
		</extension>
	</extensions>
</build>

<distributionManagement>
	<repository>
		<id>s3repo</id>
		<name>releases on s3</name>
		<url>s3://{Your bucket name}/releases/</url>
	</repository>
	<snapshotRepository>
		<id>s3repo</id>
		<name>snapshots on s3</name>
		<url>s3://{Your bucket name}/snapshots/</url>
	</snapshotRepository>
</distributionManagement>
```

# Authorization

* use any setup method supported by awssdk [https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/setup.html]
* maven standard auth NOT supported yet, maybe next release ;)


# IAM Permissions

``` json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "s3:ListBucket",
            "Resource": "arn:{aws or aws-cn}:s3:::{Your bucket name}"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject"
            ],
            "Resource": [
                "arn:{aws or aws-cn}:s3:::{Your bucket name}/releases/*",
                "arn:{aws or aws-cn}:s3:::{Your bucket name}/snapshots/*"
            ]
        }
    ]
}
```

# ChangeLog Release Notes

## v1.0.0

* initial release
