/*
 * Copyright  2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.JavaEnvUtils;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * JUnit 3 testcases for org.apache.tools.ant.DirectoryScanner
 *
 */
public class DirectoryScannerTest extends BuildFileTest {

    public DirectoryScannerTest(String name) {super(name);}

    // keep track of what operating systems are supported here.
    private boolean supportsSymlinks = Os.isFamily("unix");

    public void setUp() {
        configureProject("src/etc/testcases/core/directoryscanner.xml");
        getProject().executeTarget("setup");
    }

    public void tearDown() {
        getProject().executeTarget("cleanup");
    }

    public void test1() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha"});
        ds.scan();
        compareFiles(ds, new String[] {} ,new String[] {"alpha"});
    }

    public void test2() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }

    public void test3() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"", "alpha", "alpha/beta",
                                   "alpha/beta/gamma"});
    }

    public void testFullPathMatchesCaseSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/GAMMA.XML"});
        ds.scan();
        compareFiles(ds, new String[] {}, new String[] {});
    }

    public void testFullPathMatchesCaseInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setCaseSensitive(false);
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/GAMMA.XML"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
            new String[] {});
    }

    public void test2ButCaseInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"ALPHA/"});
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }

    public void testAllowSymlinks() {
        if (!supportsSymlinks) {
            return;
        }

        getProject().executeTarget("symlink-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha/beta/gamma"});
    }

    public void testProhibitSymlinks() {
        if (!supportsSymlinks) {
            return;
        }

        getProject().executeTarget("symlink-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/"});
        ds.setFollowSymlinks(false);
        ds.scan();
        compareFiles(ds, new String[] {}, new String[] {});
    }

    // father and child pattern test
    public void testOrderOfIncludePatternsIrrelevant() {
        String [] expectedFiles = {"alpha/beta/beta.xml",
                                   "alpha/beta/gamma/gamma.xml"};
        String [] expectedDirectories = {"alpha/beta", "alpha/beta/gamma" };
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/be?a/**", "alpha/beta/gamma/"});
        ds.scan();
        compareFiles(ds, expectedFiles, expectedDirectories);
        // redo the test, but the 2 include patterns are inverted
        ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/", "alpha/be?a/**"});
        ds.scan();
        compareFiles(ds, expectedFiles, expectedDirectories);
    }

    public void testPatternsDifferInCaseScanningSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }

    public void testPatternsDifferInCaseScanningInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/"});
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }

    public void testFullpathDiffersInCaseScanningSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/beta/gamma/gamma.xml",
            "alpha/beta/gamma/GAMMA.XML"
        });
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {});
    }

    public void testFullpathDiffersInCaseScanningInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/beta/gamma/gamma.xml",
            "alpha/beta/gamma/GAMMA.XML"
        });
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {});
    }

    public void testParentDiffersInCaseScanningSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/beta/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }

    public void testParentDiffersInCaseScanningInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/beta/"});
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }

    /**
     * Test case for setFollowLinks() and associated funtionality.
     * Only supports test on linux, at the moment because Java has
     * no real notion of symlinks built in, so an os-specfic call
     * to Runtime.exec() must be made to create a link to test against.
     */

    public void testSetFollowLinks() {
        if (supportsSymlinks) {
            try {
                // add conditions and more commands as soon as the need arises
                String[] command = new String[] {
                    "ln", "-s", "ant", "src/main/org/apache/tools/ThisIsALink"
                };
                try {
                    Runtime.getRuntime().exec(command);
                    // give ourselves some time for the system call
                    // to execute... tweak if you have a really over
                    // loaded system.
                    Thread.sleep(1000);
                } catch (IOException ioe) {
                    fail("IOException making link "+ioe);
                } catch (InterruptedException ie) {
                }

                File dir = new File("src/main/org/apache/tools");
                DirectoryScanner ds = new DirectoryScanner();

                // followLinks should be true by default, but if this ever
                // changes we will need this line.
                ds.setFollowSymlinks(true);

                ds.setBasedir(dir);
                ds.setExcludes(new String[] {"ant/**"});
                ds.scan();

                boolean haveZipPackage = false;
                boolean haveTaskdefsPackage = false;

                String[] included = ds.getIncludedDirectories();
                for (int i=0; i<included.length; i++) {
                    if (included[i].equals("zip")) {
                        haveZipPackage = true;
                    } else if (included[i].equals("ThisIsALink"
                                                  + File.separator
                                                  + "taskdefs")) {
                        haveTaskdefsPackage = true;
                    }
                }

                // if we followed the symlink we just made we should
                // bypass the excludes.

                assertTrue("(1) zip package included", haveZipPackage);
                assertTrue("(1) taskdefs package included",
                           haveTaskdefsPackage);


                ds = new DirectoryScanner();
                ds.setFollowSymlinks(false);

                ds.setBasedir(dir);
                ds.setExcludes(new String[] {"ant/**"});
                ds.scan();

                haveZipPackage = false;
                haveTaskdefsPackage = false;
                included = ds.getIncludedDirectories();
                for (int i=0; i<included.length; i++) {
                    if (included[i].equals("zip")) {
                        haveZipPackage = true;
                    } else if (included[i].equals("ThisIsALink"
                                                  + File.separator
                                                  + "taskdefs")) {
                        haveTaskdefsPackage = true;
                    }
                }
                assertTrue("(2) zip package included", haveZipPackage);
                assertTrue("(2) taskdefs package not included",
                           !haveTaskdefsPackage);

            } finally {
                File f = new File("src/main/org/apache/tools/ThisIsALink");
                if (!f.delete()) {
                    throw new RuntimeException("Failed to delete " + f);
                }
            }
        }
    }
    public void testExcludeOneFile() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "**/*.xml"
        });
        ds.setExcludes(new String[] {
            "alpha/beta/b*xml"
        });
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {});
    }
    public void testExcludeHasPrecedence() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/**"
        });
        ds.setExcludes(new String[] {
            "alpha/**"
        });
        ds.scan();
        compareFiles(ds, new String[] {},
                     new String[] {});

    }
    public void testAlternateIncludeExclude() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/**",
            "alpha/beta/gamma/**"
        });
        ds.setExcludes(new String[] {
            "alpha/beta/**"
        });
        ds.scan();
        compareFiles(ds, new String[] {},
                     new String[] {"alpha"});

    }
    public void testAlternateExcludeInclude() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {
            "alpha/**",
            "alpha/beta/gamma/**"
        });
        ds.setIncludes(new String[] {
            "alpha/beta/**"
        });
        ds.scan();
        compareFiles(ds, new String[] {},
                     new String[] {});

    }
    /**
     * Test inspired by Bug#1415.
     */
    public void testChildrenOfExcludedDirectory() {
        getProject().executeTarget("children-of-excluded-dir-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {"alpha/**"});
        ds.setFollowSymlinks(false);
        ds.scan();
        compareFiles(ds, new String[] {"delta/delta.xml"},
                    new String[] {"", "delta"});

        ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {"alpha"});
        ds.setFollowSymlinks(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml",
                                        "delta/delta.xml"},
                     new String[] {"", "alpha/beta", "alpha/beta/gamma", "delta"});

    }

    private void compareFiles(DirectoryScanner ds, String[] expectedFiles,
                              String[] expectedDirectories) {
        String includedFiles[] = ds.getIncludedFiles();
        String includedDirectories[] = ds.getIncludedDirectories();
        assertEquals("file present: ", expectedFiles.length,
                     includedFiles.length);
        assertEquals("directories present: ", expectedDirectories.length,
                     includedDirectories.length);

        TreeSet files = new TreeSet();
        for (int counter=0; counter < includedFiles.length; counter++) {
            files.add(includedFiles[counter].replace(File.separatorChar, '/'));
        }
        TreeSet directories = new TreeSet();
        for (int counter=0; counter < includedDirectories.length; counter++) {
            directories.add(includedDirectories[counter]
                            .replace(File.separatorChar, '/'));
        }

        String currentfile;
        Iterator i = files.iterator();
        int counter = 0;
        while (i.hasNext()) {
            currentfile = (String) i.next();
            assertEquals(expectedFiles[counter], currentfile);
            counter++;
        }
        String currentdirectory;
        Iterator dirit = directories.iterator();
        counter = 0;
        while (dirit.hasNext()) {
            currentdirectory = (String) dirit.next();
            assertEquals(expectedDirectories[counter], currentdirectory);
            counter++;
        }
    }

}
