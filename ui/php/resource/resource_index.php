<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : resource_index.php                                           //
//     - Desc :  Resource Index File                                         //
// 2005-08-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields      -- show the resource search form
// - search          -- search fields      -- show the result set of search
// - new             --                    -- show the new resource form
// - detailconsult   -- $param_resource    -- show the resource detail
// - detailupdate    -- $param_resource    -- show the resource detail form
// - insert          -- form fields        -- insert the resource 
// - update          -- form fields        -- update the resource 
// - check_delete    -- $param_resource    -- check links before delete
// - delete          -- $param_resource    -- delete the resource 
// - rights_admin    -- access rights screen
// - rights_update   -- Update resource agenda access rights
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple resources (ret id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$extra_css = "resource.css";
$module = "resource";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_resource_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("resource_display.inc");
require("resource_query.inc");
require("resource_js.inc");
require("$obminclude/lib/right.inc");

$uid = $auth->auth["uid"];

get_resource_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "Resource", $action, $params["resource_id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("resource", $params["resource_id"], $action);
}
page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_ids") {
  $display["search"] = html_resource_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_resource_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resource_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_resource_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_resource_search_form($params);
  $display["result"] = dis_resource_search_list($params);

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_resource_form("",$params);

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resource_consult($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_resource_detail($params["resource_id"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_resource_form($obm_q, $params);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_data_form("", $params)) {
    // If the context (same resource) was confirmed ok, we proceed
    $rid = run_query_resource_insert($params);
    if ($rid > 0) {
      $params["resource_id"] = $rid;
      $display["msg"] .= display_ok_msg("$l_resource : $l_insert_ok");
      $display["detail"] = dis_resource_consult($params);
    } else {
      $display["msg"] .= display_err_msg("$l_resource : $l_insert_error");
      $display["search"] = html_resource_search_form($params);
    }
  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_resource_form("", $params);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_data_form($params["resource_id"], $params)) {
    $retour = run_query_resource_update($params["resource_id"], $params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_resource : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_resource : $l_update_error");
    }
    $display["detail"] = dis_resource_consult($params);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"] = html_resource_form("", $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_can_delete($params["resource_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_resource_can_delete($params["resource_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_resource_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_resource_can_delete($params["resource_id"])) {
    $retour = run_query_resource_delete($params["resource_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_resource : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_resource : $l_delete_error");
    }
    $display["search"] = html_resource_search_form($params);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_resource_consult($params);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_resource_right_dis_admin($params["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  if (of_right_update_right($params, "resource")) {
    $display["msg"] .= display_ok_msg($l_right_update_ok);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
  }
  $display["detail"] = dis_resource_right_dis_admin($params["entity_id"]);

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "resource", 1);
  $display["detail"] = dis_resource_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "resource", 1);
  $display["detail"] = dis_resource_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "resource", 1);
  $display["detail"] = dis_resource_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_resource);
if (! $params["popup"]) {
  update_resource_action();
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Resource parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_resource_params() {
  
  // Get global params
  $params = get_global_params("Resource");

  //Get resource specific params
  if (isset ($params["ext_id"])) $params["resource_id"] = $params["ext_id"];
  if ((isset ($params["entity_id"])) && (! isset($params["resource_id"]))) {
    $params["resource_id"] = $params["entity_id"];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_resource_action() {
  global $params, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin,$l_header_reset,$l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["resource"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/resource/resource_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Get Ids
  $actions["resource"]["ext_get_ids"] = array (
    'Url'      => "$path/resource/resource_index.php?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('none'),
    'popup' => 1
                                    );

// New
  $actions["resource"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/resource/resource_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','insert','update','admin','detailconsult','reset','display','rights_admin','rights_update') 
                                  );

// Search
  $actions["resource"]["search"] = array (
    'Url'      => "$path/resource/resource_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );
  
  // Get resource id from external window (js)
  $actions["resource"]["getsearch"] = array (
    'Url'      => "$path/resource/resource_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );
// Detail Consult
  $actions["resource"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/resource/resource_index.php?action=detailconsult&amp;resource_id=".$params["resource_id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'rights_admin', 'rights_update') 
                                  );
// Detail Update
  $actions["resource"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/resource/resource_index.php?action=detailupdate&amp;resource_id=".$params["resource_id"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'insert', 'update')
                                     	   );

// Insert
  $actions["resource"]["insert"] = array (
    'Url'      => "$path/resource/resource_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["resource"]["update"] = array (
    'Url'      => "$path/resource/resource_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["resource"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/resource/resource_index.php?action=check_delete&amp;resource_id=".$params["resource_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'insert')
                                     	      );

// Delete
  $actions["resource"]["delete"] = array (
    'Url'      => "$path/resource/resource_index.php?action=delete&amp;resource_id=".$params["resource_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Rights Admin.
  $actions["resource"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/resource/resource_index.php?action=rights_admin&amp;entity_id=".$params["resource_id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult')
                                     );

// Rights Update
  $actions["resource"]["rights_update"] = array (
    'Url'      => "$path/resource/resource_index.php?action=rights_update&amp;entity_id=".$params["resource_id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                     );

// Admin
  $actions["resource"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/resource/resource_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                     );

// Display
  $actions["resource"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/resource/resource_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                         );
// Display
  $actions["resource"]["dispref_display"] = array (
    'Url'      => "$path/resource/resource_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );
// Display
  $actions["resource"]["dispref_level"] = array (
    'Url'      => "$path/resource/resource_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                         );

}


///////////////////////////////////////////////////////////////////////////////
// Resource Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_resource_action() {
  global $params, $actions, $path;

  // Detail Consult
  $actions["resource"]["detailconsult"]["Url"] = "$path/resource/resource_index.php?action=detailconsult&amp;resource_id=".$params["resource_id"];

  // Detail Update
  $actions["resource"]["detailupdate"]["Url"] = "$path/resource/resource_index.php?action=detailupdate&amp;resource_id=".$params["resource_id"];

  // Check Delete
  $actions["resource"]["check_delete"]["Url"] = "$path/resource/resource_index.php?action=check_delete&amp;resource_id=".$params["resource_id"];

}

?>