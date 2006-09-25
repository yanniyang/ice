// **********************************************************************
//
// Copyright (c) 2003-2006 ZeroC, Inc. All rights reserved.
//
// This copy of Ice-E is licensed to you under the terms described in the
// ICEE_LICENSE file included in this distribution.
//
// **********************************************************************

public class AllTests
{
    private static void
    test(boolean b)
    {
        if(!b)
        {
            throw new RuntimeException();
        }
    }

    public static Test.MyClassPrx
    allTests(Ice.Communicator communicator, Ice.InitializationData initData, java.io.PrintStream out)
    {
        out.print("testing stringToProxy... ");
        out.flush();
        String ref = communicator.getProperties().getPropertyWithDefault("Test.Proxy", 
		"test:default -p 12010 -t 10000");
        Ice.ObjectPrx base = communicator.stringToProxy(ref);
        test(base != null);
        out.println("ok");

	out.print("testing proxy methods... ");
	out.flush();
	test(communicator.identityToString(
		 base.ice_identity(communicator.stringToIdentity("other")).ice_getIdentity()).equals("other"));
	test(base.ice_facet("facet").ice_getFacet().equals("facet"));
	test(base.ice_twoway().ice_isTwoway());
	test(base.ice_oneway().ice_isOneway());
	test(base.ice_batchOneway().ice_isBatchOneway());
	out.println("ok");

    	out.print("testing ice_getCommunicator... ");
	out.flush();
	test(base.ice_getCommunicator() == communicator);
	out.println("ok");

	System.out.print("testing proxy comparison... ");
	System.out.flush();

	test(communicator.stringToProxy("foo").equals(communicator.stringToProxy("foo")));
	test(!communicator.stringToProxy("foo").equals(communicator.stringToProxy("foo2")));

	Ice.ObjectPrx compObj = communicator.stringToProxy("foo");

	test(compObj.ice_facet("facet").equals(compObj.ice_facet("facet")));
	test(!compObj.ice_facet("facet").equals(compObj.ice_facet("facet1")));

	test(compObj.ice_oneway().equals(compObj.ice_oneway()));
	test(!compObj.ice_oneway().equals(compObj.ice_twoway()));

	test(compObj.ice_timeout(20).equals(compObj.ice_timeout(20)));
	test(!compObj.ice_timeout(10).equals(compObj.ice_timeout(20)));

	Ice.ObjectPrx compObj1 = communicator.stringToProxy("foo:tcp -h 127.0.0.1 -p 10000");
	Ice.ObjectPrx compObj2 = communicator.stringToProxy("foo:tcp -h 127.0.0.1 -p 10001");
	test(!compObj1.equals(compObj2));

	compObj1 = communicator.stringToProxy("foo@MyAdapter1");
	compObj2 = communicator.stringToProxy("foo@MyAdapter2");
	test(!compObj1.equals(compObj2));

	compObj1 = communicator.stringToProxy("foo:tcp -h 127.0.0.1 -p 1000");
	compObj2 = communicator.stringToProxy("foo@MyAdapter1");
	test(!compObj1.equals(compObj2));

	//
	// TODO: Ideally we should also test comparison of fixed proxies.
	//
	System.out.println("ok");

        out.print("testing checked cast... ");
        out.flush();
        Test.MyClassPrx cl = Test.MyClassPrxHelper.checkedCast(base);
        test(cl != null);
        Test.MyDerivedClassPrx derived = Test.MyDerivedClassPrxHelper.checkedCast(cl);
        test(derived != null);
        test(cl.equals(base));
        test(derived.equals(base));
        test(cl.equals(derived));
        out.println("ok");

	out.print("testing checked cast with context... ");
	out.flush();
        String cref = communicator.getProperties().getPropertyWithDefault("Test.ProxyWithContext", 
		"context:default -p 12010 -t 10000");
	Ice.ObjectPrx cbase = communicator.stringToProxy(cref);
	test(cbase != null);

	Test.TestCheckedCastPrx tccp = Test.TestCheckedCastPrxHelper.checkedCast(cbase);
	java.util.Hashtable c = tccp.getContext();
	test(c == null || c.size() == 0);

	c = new java.util.Hashtable();
	c.put("one", "hello");
	c.put("two", "world");
	tccp = Test.TestCheckedCastPrxHelper.checkedCast(cbase, c);
	java.util.Hashtable c2 = tccp.getContext();
	test(IceUtil.Hashtable.equals(c, c2));
	out.println("ok");

	out.print("testing timeout... ");
	out.flush();
	try
	{
	    Test.MyClassPrx clTimeout = Test.MyClassPrxHelper.uncheckedCast(cl.ice_timeout(500));
	    clTimeout.opSleep(1000);
	    test(false);
	}
	catch(Ice.TimeoutException ex)
	{
	}
	out.println("ok");

        out.print("testing twoway operations... ");
        out.flush();
        Twoways.twoways(communicator, initData, cl);
        Twoways.twoways(communicator, initData, derived);
        derived.opDerived();
        out.println("ok");
	out.print("testing batch oneway operations... ");
	out.flush();
	BatchOneways.batchOneways(cl);
	BatchOneways.batchOneways(derived);
	out.println("ok");

        return cl;
    }
}
