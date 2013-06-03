# Wicket Routes Mount [![Build Status](https://buildhive.cloudbees.com/job/code-troopers/job/wicket-routes-mount/badge/icon)](https://buildhive.cloudbees.com/job/code-troopers/job/wicket-routes-mount/)

[Apache Wicket](http://wicket.apache.org) is a Java Web framework providing clean separation between markup and logic.

This project allows to group page mounting in a single file. It also permits to set permissions on mounts.

# Setup on your project

Add the following Maven dependency (available on Maven Central)

    <dependency>
        <groupId>com.code-troopers</groupId>
        <artifactId>wicket-route-mount</artifactId>
        <version>0.2</version>
    </dependency>
    
## Standard usage

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

    
## Regular expression parameter checking

If you want you can use the built-in mecanism allowing to check your parameters using regular expressions. This way you can easily define only numerical parameters or restriction over special values. When you want to use this, just add a `:` followed by the regular expression at the end of your param like this : `${param:[0-0]+}`. Your `routes.conf` can look like the following :

    # mountPoint            class                           roles
    /user/${id:[0-9]+}      codetroopers.UserPage           

## Package mounting
If you want to mount all the pages in a package, you can put the packageName after the mounting point in your `routes.conf` file. When reading the file, all the pages in the package will be mounted under the specified mount point (the standard `PackageMapper` is used).


# Bug tracker

Have a bug? Please create an issue here on GitHub!

https://github.com/code-troopers/wicket-routes-mount/issues


# Special notes

The implementation provided here is open for pull request or further integration into WicketStuff.

Thanks to Cloudbees buildhive for providing a free Jenkins instance.

Thanks to 55Minutes.com for providing the initial implementation of `ParamCheckingPatternMapper`.

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
