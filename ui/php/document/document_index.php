<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : document_index.php                                           //
//     - Desc : Document Index File                                          //
// 2003-08-21 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the document search form
// - search             -- search fields  -- show the result set of search
///////////////////////////////////////////////////////////////////////////////

// vues (par societe,...)
// migration ??? ou outil d'import d'un depot (repertoire classique)
// acl

$path = "..";
$module = "document";
$obminclude = getenv("OBM_INCLUDE_VAR");
$extra_css = "document.css";
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_document_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("document_query.inc");
require("document_display.inc");
require_once("document_js.inc");
require_once("$obminclude/of/of_category.inc");

get_document_action();
$perm->check_permissions($module, $action);
if (! check_privacy($module, "Document", $action, $params["document_id"], $uid)) {
  $display["msg"] = display_err_msg($l_error_visibility);
  $action = "index";
} else {
  update_last_visit("document", $params["document_id"], $action);
}
page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_path") {
  $display["detail"] = html_document_tree($params, $ext_disp_file);

} elseif ($action == "accessfile") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["document_id"] > 0) {
    $doc_q = run_query_document_detail($params["document_id"]);
    if ($doc_q->num_rows() == 1) {
      dis_document_file($doc_q);
      exit();
    }
  } else {
    $display["msg"] .= display_err_msg("$l_no_document !");
  }  

} elseif ($action == "ext_get_ids") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_document_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_document_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_document_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_document_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_document_search_form($params);
  $display["result"] = dis_document_search_list($params);
  
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_document_form($action, $params, "");
  
} elseif ($action == "new_dir") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_document_dir_form($action, $params);
  
} elseif ($action == "detailupdate_dir") {
///////////////////////////////////////////////////////////////////////////////
  $doc_q = run_query_document_detail($params["document_id"]);
  $display["detail"] = html_document_dir_form($action, $params,$doc_q);

} elseif ($action == "tree") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_document_tree($params,"true");
  
} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_document_consult($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["document_id"] > 0) {
    $doc_q = run_query_document_detail($params["document_id"]);
    if ($doc_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($doc_q);
      $display["detail"] = dis_document_form($action, $params, $doc_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_data_form("", $params)) {
    $params["document_id"] = run_query_document_insert($params);
    echo "p1=".$params["document_id"];
    if ($params["document_id"]) {
      update_last_visit("document", $params["document_id"], $action);
      $display["msg"] .= display_ok_msg("$l_document : $l_insert_ok");
      $display["detail"] = dis_document_consult($params);
    } else {
      $display["msg"] .= display_err_msg("$l_document : $l_insert_error $err_msg");
      $display["detail"] = dis_document_form($action, $params, "");
    }
  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_document_form($action, $params, "");
  }

} elseif ($action == "insert_dir") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_dir_data_form($params)) {
    $retour = run_query_document_insert_dir($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_dir : $l_insert_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_dir : $l_insert_error");
    }
    $display["detail"] = html_document_tree($params,true);
  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_document_dir_form($action, $params);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_data_form($params["document_id"], $params)) {
    $retour = run_query_document_update($params["document_id"], $params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_document : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_document : $l_update_error  $err_msg");
    }
    $doc_q = run_query_document_detail($params["document_id"]);
    $display["detailInfo"] .= display_record_info($doc_q);
    $display["detail"] = html_document_consult($doc_q);
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_document_form($action, $params, "");
  }

} elseif ($action == "update_dir") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_dir_data_form($params)) {
    $retour = run_query_document_update_dir($params);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_dir : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_dir : $l_update_error");
    }
    $display["detail"] = html_document_tree($params,true);
  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = html_document_dir_form($action, $params);
  }

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_can_delete($params["document_id"])) {
    $display["msg"] .= display_info_msg($err_msg);
    $display["detail"] = dis_document_can_delete($params["document_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $display["detail"] = dis_document_consult($params);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_can_delete($params["document_id"])) {
    $retour = run_query_document_delete($params["document_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_document : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_document : $l_delete_error");
    }
    $display["search"] = dis_document_search_form($params);
    $display["result"] = dis_document_search_list($params);
  } else {
    $display["msg"] .= display_warn_msg("$err_msg $l_cant_delete");
    $display["detail"] = dis_document_consult($params);
  }

} elseif ($action == "dir_check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_can_delete_dir($params["document_id"])) {
    $display["detail"] = dis_document_can_delete_dir($params["document_id"]);
  } else {
    $display["msg"] .= display_warn_msg("$err_msg $l_dir_cant_delete");
    $display["detail"] = html_document_tree($params,"true");
  }

} elseif ($action == "dir_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_document_can_delete_dir($params["document_id"])) {
    $retour = run_query_document_delete($params["document_id"]);
    if ($retour) {
      $display["msg"] .= display_ok_msg("$l_dir : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_dir : $l_delete_error");
    }
  } else {
    $display["msg"] .= display_warn_msg("$err_msg $l_dir_cant_delete");
  }
  $display["detail"] = html_document_tree($params,"true");

} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_document_admin_index();

} elseif ($action == "category1_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("document", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "category1_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("document", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_update_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "category1_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("document", "category1", $params, "mono");

} elseif ($action == "category1_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("document", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_delete_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "category2_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("document", "category2", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category2 : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category2 : $l_insert_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "category2_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("document", "category2", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category2 : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category2 : $l_update_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "category2_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("document", "category2", $params, "mono");

 } elseif ($action == "category2_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("document", "category2", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category2 : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category2 : $l_delete_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "mime_insert") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_document_mime_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_mime : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_mime : $l_insert_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "mime_update") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_document_mime_update($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_mime : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_mime : $l_update_error");
  }
  $display["detail"] .= dis_document_admin_index();

} elseif ($action == "mime_checklink") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_document_mime_links($params);

} elseif ($action == "mime_delete") {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_document_mime_delete($params["mime"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_mime : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_mime : $l_delete_error");
  }
  $display["detail"] .= dis_document_admin_index();

}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "document", 1);
  $display["detail"] = dis_document_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "document", 1);
  $display["detail"] = dis_document_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "document", 1);
  $display["detail"] = dis_document_display_pref($prefs);
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_document);
$display["end"] = display_end();
if (! $params["popup"]) {
  update_document_action();
  $display["header"] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_document_params() {

  // Get global params
  $params = get_global_params("Document");

  if (isset ($params["path"])) $params["path"] = format_path(trim($params["path"]));
  if (isset ($_FILES['fi_file'])) {
    $params["file_tmp"] = $_FILES['fi_file']["tmp_name"];
    $params["name"] = $_FILES['fi_file']['name'];
    $params["size"] = $_FILES['fi_file']['size'];
    $params["type"] = $_FILES['fi_file']['type'];
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Document Action 
///////////////////////////////////////////////////////////////////////////////
function get_document_action() {
  global $params, $actions, $path;
  global $l_header_tree, $l_header_find, $l_header_new, $l_header_consult;
  global $l_header_update,$l_header_delete;
  global $l_header_display,$l_header_admin,$l_header_new_dir;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index  
  $actions["document"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/document/document_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );

// Tree view
  $actions["document"]["tree"]  = array (
    'Name'     => $l_header_tree,
    'Url'      => "$path/document/document_index.php?action=tree",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                     		 );

// Search
  $actions["document"]["search"] = array (
    'Url'      => "$path/document/document_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["document"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/document/document_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index', 'tree','detailconsult','new_dir','insert','insert_dir', 'check_delete', 'delete', 'dir_check_delete', 'dir_delete','update','admin','display') 
                                     );

// New Dir
  $actions["document"]["new_dir"] = array (
    'Name'     => $l_header_new_dir,
    'Url'      => "$path/document/document_index.php?action=new_dir",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index', 'tree','detailconsult','new','insert','insert_dir', 'check_delete', 'delete', 'dir_check_delete', 'dir_delete','update','admin','display') 
                                     );

// Detail Consult
  $actions["document"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/document/document_index.php?action=detailconsult&amp;document_id=".$params["document_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate') 
                                     		 );

// Access Document
  $actions["document"]["accessfile"]  = array (
    'Url'      => "$path/document/document_index.php?action=accessfile&amp;document_id=".$params["document_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     		 );

// Detail Update
  $actions["document"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/document/document_index.php?action=detailupdate&amp;document_id=".$params["document_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update')
  );

// Detail Update
  $actions["document"]["detailupdate_dir"] = array (
    'Url'      => "$path/document/document_index.php?action=detailupdate&amp;document_id=".$params["document_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
  );
// Update
  $actions["document"]["update"] = array (
    'Url'      => "$path/document/document_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     	      );
// Update
  $actions["document"]["update_dir"] = array (
    'Url'      => "$path/document/document_index.php?action=update_dir",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     	      );
// Check_Delete
  $actions["document"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/document/document_index.php?action=check_delete&amp;document_id=".$params["document_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                     	      );

// Delete
  $actions["document"]["delete"] = array (
    'Url'      => "$path/document/document_index.php?action=delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Directory Check Delete
  $actions["document"]["dir_check_delete"] = array (
    'Url'      => "$path/document/document_index.php?action=dir_check_delete&amp;document_id=".$params["document_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None')
                                     	      );

// Directory Delete
  $actions["document"]["dir_delete"] = array (
    'Url'      => "$path/document/document_index.php?action=dir_delete",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                     	      );

// Insert
  $actions["document"]["insert"] = array (
    'Url'      => "$path/document/document_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );  

// Dir Insert
  $actions["document"]["insert_dir"] = array (
    'Url'      => "$path/document/document_index.php?action=insert_dir",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );  
// Admin
  $actions["document"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/document/document_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
   					);

// Category Insert
  $actions["document"]["category1_insert"] = array (
    'Url'      => "$path/document/document_index.php?action=category1_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["document"]["category1_update"] = array (
    'Url'      => "$path/document/document_index.php?action=category1_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["document"]["category1_checklink"] = array (
    'Url'      => "$path/document/document_index.php?action=category1_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["document"]["category1_delete"] = array (
    'Url'      => "$path/document/document_index.php?action=category1_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Category Insert
  $actions["document"]["category2_insert"] = array (
    'Url'      => "$path/document/document_index.php?action=category2_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Category Update
  $actions["document"]["category2_update"] = array (
    'Url'      => "$path/document/document_index.php?action=category2_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Category Check Link
  $actions["document"]["category2_checklink"] = array (
    'Url'      => "$path/document/document_index.php?action=category2_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Category Delete
  $actions["document"]["category2_delete"] = array (
    'Url'      => "$path/document/document_index.php?action=category2_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );

// Mime Insert
  $actions["document"]["mime_insert"] = array (
    'Url'      => "$path/document/document_index.php?action=mime_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Mime Update
  $actions["document"]["mime_update"] = array (
    'Url'      => "$path/document/document_index.php?action=mime_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Mime Check Link
  $actions["document"]["mime_checklink"] = array (
    'Url'      => "$path/document/document_index.php?action=mime_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Mime Delete
  $actions["document"]["mime_delete"] = array (
    'Url'      => "$path/document/document_index.php?action=mime_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
  // Display
  $actions["document"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/document/document_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions["document"]["dispref_display"] = array (
    'Url'      => "$path/document/document_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["document"]["dispref_level"]  = array (
    'Url'      => "$path/document/document_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// External path view
  $actions["document"]["ext_get_path"]  = array (
    'Url'      => "$path/document/document_index.php?action=ext_get_path",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// External view
  $actions["document"]["ext_get_ids"]  = array (
    'Url'      => "$path/document/document_index.php?action=ext_get_ids",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
}


///////////////////////////////////////////////////////////////////////////////
// Document Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_document_action() {
  global $params, $actions, $path;

  $id = $params["document_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["document"]["detailconsult"]["Url"] = "$path/document/document_index.php?action=detailconsult&amp;document_id=$id";
    $actions["document"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["document"]["detailupdate"]['Url'] = "$path/document/document_index.php?action=detailupdate&amp;document_id=$id";
    $actions["document"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["document"]["check_delete"]['Url'] = "$path/document/document_index.php?action=check_delete&amp;document_id=$id";
    $actions["document"]["check_delete"]['Condition'][] = 'insert';
  }
}


?>
