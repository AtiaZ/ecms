package org.exoplatform.ecms.upgrade35x.plf351.user_drive_ext;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.IdentityConstants;

/**
 * Migrate Personal Document drive, add symlink to users Public for users Private
 * @version $Revision$
 */
public class UserDriveExtPlugin  extends UpgradeProductPlugin {



    private static final Log LOG = ExoLogger.getLogger(UserDriveExtPlugin.class);

    private static final String SYMLINK_NAME        = "Public";

    private static final String SYMLIN_TYPE         = "exo:symlink";

    private static final String PUPLIC_DRIVE_EXT    = "/Public";

    private static final String PRIVATE_DRIVE_EXT   = "/Private";

    private final static String WORKSPACE           = "exo:workspace";

    private final static String UUID                = "exo:uuid";

    private final static String PRIMARY_TYPE        = "exo:primaryType";

    /**   RepositoryService used to get current Repository */
    private final RepositoryService repositoryService_;

    /**   LinkManager Service to create jcr symlink **/
    private final LinkManager linkManager_;

    /**   NodeHierarchyCreator Service : used to get complet path to a given user Folder */
    private NodeHierarchyCreator nodeHierarchyCreator_ ;
    
    private OrganizationService organService_;

    public UserDriveExtPlugin (RepositoryService repositoryService, LinkManager linkManager, NodeHierarchyCreator nodeHierarchyCreator, OrganizationService organService, InitParams initParams)
    {
        super(initParams);
        this.repositoryService_ = repositoryService;

        this.linkManager_ =  linkManager;

        nodeHierarchyCreator_ = nodeHierarchyCreator ;
        this.organService_ = organService;
    }

    
    @Override
    public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
      return VersionComparator.isAfter(newVersion,previousVersion);
    }
    
    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
      if(LOG.isInfoEnabled()) LOG.info("Start adding symlink to Public for Users Private folder");
 
      try {
        ListAccess<User> userList = organService_.getUserHandler().findAllUsers();
        for(int i = 0; i < userList.getSize(); i++){
          User user = null;
          try{
            user = userList.load(i, 1)[0];
            createLink(user.getUserName());
          }
          catch (Exception e) {
            LOG.error("Upgrade FAILED! for user: "+  user.getUserName(), e);
          }
        }
      } catch (Exception e) {
        LOG.info("There is not any data for migration", e);
      }
      
      if(LOG.isInfoEnabled()) LOG.info("Finished adding symlink to Public for Users Private folder");
    }
    
    private void createLink(String userName) throws Exception
    {

        SessionProvider sessionProvider = SessionProvider.createSystemProvider();


        Session session = getSession(sessionProvider);

        /** path to private user drive*/
        String privatePathUserFolder = "";

        /** path to public user drive*/
        String publicPathUserFolder = "";

        try {

            Node userNode = nodeHierarchyCreator_.getUserNode(sessionProvider,userName);
            Node publicSymlink = null;
            
            boolean existPublicSymlink = true;
            
            try{
              
             publicSymlink = userNode.getNode("Private/Public");
             
            }
            catch (PathNotFoundException e) {
              
              existPublicSymlink = false;
              
            }
            
            if( (existPublicSymlink == false) || (publicSymlink.isNodeType(SYMLIN_TYPE) == false)){

              privatePathUserFolder = userNode.getPath().concat(PRIVATE_DRIVE_EXT);
  
              publicPathUserFolder = userNode.getPath().concat(PUPLIC_DRIVE_EXT);
  
              Node symLinkParent = (Node) session.getItem(privatePathUserFolder);
  
              Node targetNode = (Node) session.getItem(publicPathUserFolder);
  
              Node linkNode = null;
  
              /**
              linkNode = linkManager_.createLink(symLinkParent,SYMLIN_TYPE,targetNode,SYMLINK_NAME);
              */
  
              linkNode = createLink(symLinkParent,SYMLIN_TYPE,targetNode,SYMLINK_NAME);
  
              ((ExtendedNode)linkNode).setPermission(IdentityConstants.ANY, new String[]{PermissionType.READ});
  
              linkNode.save();
              
              LOG.info("Successfull: Created symlink to Public in Private of user: " + userName);
            }

        } catch (Exception e) {

            LOG.error("An error occurs while processing the creation of Public Symlink " ,e);

        }  finally {

            if (session != null) {
                session.logout();
            }

            if (sessionProvider != null) {
                sessionProvider.close();
            }

        }
    }

    private Session getSession(SessionProvider sessionProvider) throws RepositoryException {

        ManageableRepository repository = repositoryService_.getCurrentRepository();

        return sessionProvider.getSession(repository.getConfiguration().getDefaultWorkspaceName(), repository);
    }

     private Node createLink(Node parent, String linkType, Node target, String linkName) throws RepositoryException {
        if (!target.isNodeType(SYMLIN_TYPE)) {
            if (target.canAddMixin("mix:referenceable")) {
                target.addMixin("mix:referenceable");
                target.getSession().save();
            }
            if (linkType == null || linkType.trim().length() == 0)
                linkType = SYMLIN_TYPE;
            if (linkName == null || linkName.trim().length() == 0)
                linkName = target.getName();
            Node linkNode = parent.addNode(linkName, linkType);
            try {
                updateAccessPermissionToLink(linkNode, target);
            } catch(Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("CAN NOT UPDATE ACCESS PERMISSIONS FROM TARGET NODE TO LINK NODE", e);
                }
            }
            linkNode.setProperty(WORKSPACE, target.getSession().getWorkspace().getName());
            linkNode.setProperty(PRIMARY_TYPE, target.getPrimaryNodeType().getName());
            linkNode.setProperty(UUID, target.getUUID());
            linkNode.getSession().save();

            return linkNode;
        }
        return null;
    }

    private boolean canChangePermission(Node node) throws RepositoryException {
        try {
            ((ExtendedNode)node).checkPermission(PermissionType.CHANGE_PERMISSION);
            return true;
        } catch(AccessControlException e) {
            return false;
        }
    }

    private void updateAccessPermissionToLink(Node linkNode, Node targetNode) throws Exception {
        if(canChangePermission(linkNode)) {
            if(linkNode.canAddMixin("exo:privilegeable")) {
                linkNode.addMixin("exo:privilegeable");
                ((ExtendedNode)linkNode).setPermission(getNodeOwner(linkNode),PermissionType.ALL);
            }
            removeCurrentIdentites(linkNode);
            Map<String, String[]> perMap = new HashMap<String, String[]>();
            List<String> permsList = new ArrayList<String>();
            List<String> idList = new ArrayList<String>();
            for(AccessControlEntry accessEntry : ((ExtendedNode)targetNode).getACL().getPermissionEntries()) {
                if(!idList.contains(accessEntry.getIdentity())) {
                    idList.add(accessEntry.getIdentity());
                    permsList = ((ExtendedNode)targetNode).getACL().getPermissions(accessEntry.getIdentity());
                    perMap.put(accessEntry.getIdentity(), permsList.toArray(new String[permsList.size()]));
                }
            }
            ((ExtendedNode)linkNode).setPermissions(perMap);
        }
    }

    private String getNodeOwner(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException {
        if(node.hasProperty("exo:owner")) {
            return node.getProperty("exo:owner").getString();
        }
        return IdentityConstants.SYSTEM;
    }
    private void removeCurrentIdentites(Node linkNode) throws AccessControlException, RepositoryException {
        String currentUser = linkNode.getSession().getUserID();
        if (currentUser != null)
            ((ExtendedNode)linkNode).setPermission(currentUser, PermissionType.ALL);
        for(AccessControlEntry accessEntry : ((ExtendedNode)linkNode).getACL().getPermissionEntries()) {
            if(canRemovePermission(linkNode, accessEntry.getIdentity())
                    && ((ExtendedNode)linkNode).getACL().getPermissions(accessEntry.getIdentity()).size() > 0
                    && !accessEntry.getIdentity().equals(currentUser)) {
                ((ExtendedNode) linkNode).removePermission(accessEntry.getIdentity());
            }
        }
    }
    private boolean canRemovePermission(Node node, String identity) throws ValueFormatException,
            PathNotFoundException, RepositoryException {
        String owner = getNodeOwner(node);
        if(identity.equals(IdentityConstants.SYSTEM)) return false;
        if(owner != null && owner.equals(identity)) return false;
        return true;
    }
}
