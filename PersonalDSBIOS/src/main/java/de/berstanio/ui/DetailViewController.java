package de.berstanio.ui;

import apple.foundation.NSURL;
import apple.foundation.NSURLRequest;
import apple.uikit.UILabel;
import apple.uikit.UIViewController;
import apple.uikit.UIWebView;
import apple.uikit.enums.UIScrollViewContentInsetAdjustmentBehavior;
import apple.webkit.WKWebView;
import apple.webkit.protocol.WKNavigationDelegate;
import apple.webkit.protocol.WKUIDelegate;
import de.berstanio.ghgparser.GHGParser;
import de.berstanio.personaldsblib.FreeRoomDSB;
import de.berstanio.personaldsblib.PersonalDSBLib;

import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.Owned;
import org.moe.natj.general.ann.RegisterOnStartup;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.IBOutlet;
import org.moe.natj.objc.ann.ObjCClassName;
import org.moe.natj.objc.ann.Property;
import org.moe.natj.objc.ann.Selector;

@org.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("DetailViewController")
@RegisterOnStartup
public class DetailViewController extends UIViewController {

    @Owned
    @Selector("alloc")
    public static native DetailViewController alloc();

    @Selector("init")
    public native DetailViewController init();

    protected DetailViewController(Pointer peer) {
        super(peer);
    }

    private String detailItem;

    public String getDetailItem() {
        return detailItem;
    }

    public void setDetailItem(String detailItem) {
        this.detailItem = detailItem;
        configureView();
    }

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();
        configureView();
    }

    private void configureView() {
        WKWebView webView = WKWebView.alloc();
        webView.init();
        //webView.setUIDelegate(new WKUIDelegate() {});
        //webView.setNavigationDelegate(new WKNavigationDelegate() {});
        setView(webView);
        webView.loadHTMLStringBaseURL("<html><body><p>Hello!</p></body></html>", null);
        //webView.setAllowsBackForwardNavigationGestures(false);
        //webView.scrollView().setBounces(false);
        //webView.scrollView().setContentInsetAdjustmentBehavior(UIScrollViewContentInsetAdjustmentBehavior.Never);

    }


}
