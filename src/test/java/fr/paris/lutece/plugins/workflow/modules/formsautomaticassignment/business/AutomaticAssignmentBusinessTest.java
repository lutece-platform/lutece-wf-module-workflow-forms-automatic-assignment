package fr.paris.lutece.plugins.workflow.modules.formsautomaticassignment.business;

import java.util.List;

import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.test.LuteceTestCase;

public class AutomaticAssignmentBusinessTest extends LuteceTestCase
{

    private IAutomaticAssignmentDAO _dao = SpringContextService.getBean( AutomaticAssignmentDAO.BEAN_NAME );
    private Plugin _plugin = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
    
    public void testCRUD( )
    {
        AutomaticAssignment assignment = new AutomaticAssignment( );
        assignment.setIdField( 1 );
        assignment.setIdQuestion( 2 );
        assignment.setIdTask( 3 );
        assignment.setFieldValue( "strFieldValue" );
        assignment.setWorkgroupKey( "strWorkgroupKey" );
        
        _dao.insert( assignment, _plugin );
        
        assertTrue( _dao.checkExist( assignment, _plugin ) );
        
        List<AutomaticAssignment> list = _dao.loadByTask( 3, _plugin );
        assertEquals( 1, list.size( ) );
        AutomaticAssignment loaded = list.get( 0 );
        assertEquals( assignment.getIdField( ), loaded.getIdField( ) );
        assertEquals( assignment.getIdQuestion( ), loaded.getIdQuestion( ) );
        assertEquals( assignment.getIdField( ), loaded.getIdField( ) );
        assertEquals( assignment.getWorkgroupKey( ), loaded.getWorkgroupKey( ) );
        
        _dao.delete( assignment, _plugin );
        list = _dao.loadByTask( 3, _plugin );
        assertEquals( 0, list.size( ) );
    }
}
