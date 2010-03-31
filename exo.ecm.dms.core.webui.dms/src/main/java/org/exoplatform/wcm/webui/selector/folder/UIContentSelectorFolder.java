package org.exoplatform.wcm.webui.selector.folder;

import org.exoplatform.wcm.webui.selector.content.UIContentSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Author : TAN DUNG DANG
 * dzungdev@gmail.com
 * Jan 20, 2009
 */

@ComponentConfig(
    template = "system:/groovy/webui/core/UITabPane_New.gtmpl"
)

public class UIContentSelectorFolder extends UIContentSelector {

  /**
   * Instantiates a new uI content selector folder.
   * 
   * @throws Exception the exception
   */
  public UIContentSelectorFolder() throws Exception {
    addChild(UIContentBrowsePanelFolder.class, null, null);
    setSelectedTab(1);
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    getChild(UIContentBrowsePanelFolder.class).init();
  }
}
