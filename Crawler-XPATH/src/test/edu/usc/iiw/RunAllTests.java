package test.edu.usc.iiw;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
         Class.forName("test.edu.usc.iiw.TestXPathEngineImpl"),
         Class.forName("test.edu.usc.iiw.TestDBWrapper"),
         Class.forName("test.edu.usc.iiw.TestRobotsTxtInfo"),
         Class.forName("test.edu.usc.iiw.TestXPathCrawler"),
         Class.forName("test.edu.usc.iiw.TestCrawlerHTTPClient")
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
