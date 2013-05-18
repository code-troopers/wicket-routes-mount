# Wicket Routes Mount [![Build Status](https://buildhive.cloudbees.com/job/code-troopers/job/wicket-routes-mount/badge/icon)](https://buildhive.cloudbees.com/job/code-troopers/job/wicket-routes-mount/)

[Apache Wicket](http://wicket.apache.org) is a Java Web framework providing clean separation between markup and logic.

This project allows to group page mounting in a single file. It also permits to set permissions on mounts.

# Setup on your project

Add the following Maven dependency (available on Maven Central)

    <dependency>
        <groupId>com.code-troopers</groupId>
        <artifactId>wicket-route-mount</artifactId>
        <version>0.1</version>
    </dependency>

Then you will need to write your mounts in a `routes.conf` located at the root of your sources (typically `src/main/resources/routes.conf`)
 
The content of your file must look like the following (standard Wicket mount syntax applies : `${requiredParam}` `#{optionalParam}`) : 

    # mountPoint            class                           roles
    /home                   codetroopers.HomePage           
    /secured                codetroopers.SecuredPage        USER
    
To parse the file and mount pages, you will need to add the following code in your `Application#init()` method
    
    @Override
    public void init(){
        super.init();
        RoutesMountParser.mount(this);
    }

# Bug tracker

Have a bug? Please create an issue here on GitHub!

https://github.com/code-troopers/wicket-routes-mount/issues


# Special notes

The implementation provided here is open for pull request or further integration into WicketStuff.

Thanks to Cloudbees buildhive for providing a free Jenkins instance.

# Copyright and license

Copyright 2013 Code-Troopers.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
