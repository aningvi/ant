/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;

/**
 * Condition that tests whether a given property has been set.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @version $Revision$
 */
public class IsSet extends ProjectComponent implements Condition
{
    private String property;

    public void setProperty( String p )
    {
        property = p;
    }

    public boolean eval()
        throws BuildException
    {
        if( property == null )
        {
            throw new BuildException( "No property specified for isset condition" );
        }

        return getProject().getProperty( property ) != null;
    }

}
