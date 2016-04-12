Cloud Connector Sample Application
==================================

This example demonstrates the usage of the Cloud Connector API. Users can manage
the connection status of the service as well as configure several of its 
parameters. It is also possible to receive device requests from Device Cloud and
upload any type of data point.

Demo requirements
-----------------

To run this example you need:

* One compatible device to host the application.
* A USB connection between the device and the host PC in order to transfer and
  launch the application.

Demo setup
----------

Make sure the hardware is set up correctly:

1. The device is powered on.
2. The device is connected directly to the PC by the micro USB cable.

Demo run
--------

The example is already configured, so all you need to do is to build and launch 
the project.

Once application starts, configure the basic Cloud Connector service parameters
such as device name, description, contact information and vendor ID. The rest of
parameters are configured by default. Click save to store the new values.

Device requests can be received from Device Cloud using the target name 
"cloud_connector_sample".

Finally, you can send any type of data point to Device Cloud by clicking on 
"Data Points" button. In the new dialog configure the desired data point format 
and number and click "Send" button.

Compatible with
---------------

* ConnectCore 6 SBC
* ConnectCore 6 SBC v3

License
-------

Copyright (c) 2014-2016, Digi International Inc. <support@digi.com>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
