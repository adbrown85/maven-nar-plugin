package org.apache.maven.plugin.nar;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Generates a NarSystem class with static methods to use inside the java part of the library.
 * 
 * @goal nar-system-generate
 * @phase generate-sources
 * @requiresProject
 * @author Mark Donszelmann
 */
public class NarSystemMojo
    extends AbstractCompileMojo
{

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( shouldSkip() )
            return;

        // get packageName if specified for JNI.
        String packageName = null;
        String narSystemName = null;
        File narSystemDirectory = null;
        boolean jniFound = false;
        for ( Iterator i = getLibraries().iterator(); !jniFound && i.hasNext(); )
        {
            Library library = (Library) i.next();
            if ( library.getType().equals( Library.JNI ) )
            {
                packageName = library.getNarSystemPackage();
                narSystemName = library.getNarSystemName();
                narSystemDirectory = new File(getTargetDirectory(), library.getNarSystemDirectory());
                jniFound = true;
            }
        }
        
        if ( !jniFound || packageName == null)
            return;

        // make sure destination is there
        narSystemDirectory.mkdirs();

        getMavenProject().addCompileSourceRoot( narSystemDirectory.getPath() );

        File fullDir = new File( narSystemDirectory, packageName.replace( '.', '/' ) );
        fullDir.mkdirs();

        File narSystem = new File( fullDir, narSystemName + ".java" );
        getLog().info("Generating "+narSystem);
        try
        {
            String artifactId = getMavenProject().getArtifactId();
            String version = getMavenProject().getVersion();
            FileOutputStream fos = new FileOutputStream( narSystem );
            PrintWriter p = new PrintWriter( fos );
            p.println( "// DO NOT EDIT: Generated by NarSystemGenerate." );
            p.println( "package " + packageName + ";" );
            p.println( "" );
            p.println( "/**" );
            p.println( " * Generated class to load the correct version of the jni library" );            
            p.println( " *" );            
            p.println( " * @author maven-nar-plugin" );            
            p.println( " */" );            
            p.println( "public class NarSystem" );
            p.println( "{" );
            p.println( "" );
            p.println( "    private NarSystem() " );
            p.println( "    {" );
            p.println( "    }" );
            p.println( "" );
            p.println( "   /**" );
            p.println( "    * Load jni library: "+artifactId+"-"+version );            
            p.println( "    *" );            
            p.println( "    * @author maven-nar-plugin" );            
            p.println( "    */" );            
            p.println( "    public static void loadLibrary()" );
            p.println( "    {" );
            p.println( "        System.loadLibrary(\"" + artifactId + "-"
                + version + "\");" );
            p.println( "    }" );
            p.println( "}" );
            p.close();
            fos.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not write '" + narSystemName + "'", e );
        }
    }
}
