# UI Backup Agent

A micro service that is used to regularly back up company files to AWS cloud.


## Basic Configuration

You need to set up your AWS security credentials before the sample code is able
to connect to AWS. An example is provided as `src/main/resources/AwsCredentials.properties`.
Copy the example to `src/main/resources/AwsCredentials.properties` and edit to
use your access and secret keys:

    accessKey = <your access key id>
    secretKey = <your secret key>

See the [Security Credentials](http://aws.amazon.com/security-credentials) page
for more information on getting your keys.


## Service

Use Docker to start the service with the following command:

``                                                      ``
