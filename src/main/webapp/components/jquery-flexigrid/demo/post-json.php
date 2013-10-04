<?php
$page = isset($_POST['page']) ? $_POST['page'] : 1;
$rp = isset($_POST['rp']) ? $_POST['rp'] : 10;
$sortname = isset($_POST['sortname']) ? $_POST['sortname'] : 'name';
$sortorder = isset($_POST['sortorder']) ? $_POST['sortorder'] : 'desc';
$query = isset($_POST['query']) ? $_POST['query'] : false;
$qtype = isset($_POST['qtype']) ? $_POST['qtype'] : false;

/* -- To use the SQL, remove this block
$usingSQL = true;
function runSQL($rsql) {

	$db['default']['hostname'] = "localhost";
	$db['default']['username'] = '';
	$db['default']['password'] = "";
	$db['default']['database'] = "";

	$db['live']['hostname'] = 'localhost';
	$db['live']['username'] = '';
	$db['live']['password'] = '';
	$db['live']['database'] = '';

	$active_group = 'default';

	$base_url = "http://".$_SERVER['HTTP_HOST'];
	$base_url .= str_replace(basename($_SERVER['SCRIPT_NAME']),"",$_SERVER['SCRIPT_NAME']);

	$connect = mysql_connect($db[$active_group]['hostname'],$db[$active_group]['username'],$db[$active_group]['password']) or die ("Error: could not connect to database");
	$db = mysql_select_db($db[$active_group]['database']);

	$result = mysql_query($rsql) or die ($rsql);
	return $result;
	mysql_close($connect);
}

function countRec($fname,$tname) {
	$sql = "SELECT count($fname) FROM $tname ";
	$result = runSQL($sql);
	while ($row = mysql_fetch_array($result)) {
		return $row[0];
	}
}

$sort = "ORDER BY $sortname $sortorder";
$start = (($page-1) * $rp);

$limit = "LIMIT $start, $rp";

$where = "";
if ($query) $where = " WHERE $qtype LIKE '%".mysql_real_escape_string($query)."%' ";

$sql = "SELECT iso,name,printable_name,iso3,numcode FROM country $where $sort $limit";
$result = runSQL($sql);

$total = countRec("iso","country $where");
*/
if(!isset($usingSQL)){
	include dirname(__FILE__).'/countryArray.inc.php';
	if($qtype && $query){
		$query = strtolower(trim($query));
		foreach($rows AS $key => $row){
			if(strpos(strtolower($row[$qtype]),$query) === false){
				unset($rows[$key]);
			}
		}
	}
	//Make PHP handle the sorting
	$sortArray = array();
	foreach($rows AS $key => $row){
		$sortArray[$key] = $row[$sortname];
	}
	$sortMethod = SORT_ASC;
	if($sortorder == 'desc'){
		$sortMethod = SORT_DESC;
	}
	array_multisort($sortArray, $sortMethod, $rows);
	$total = count($rows);
	$rows = array_slice($rows,($page-1)*$rp,$rp);
}
header("Content-type: application/json");
$jsonData = array('page'=>$page,'total'=>$total,'rows'=>array());
foreach($rows AS $row){
	//If cell's elements have named keys, they must match column names
	//Only cell's with named keys and matching columns are order independent.
	$entry = array('id'=>$row['iso'],
		'cell'=>array(
			'name'=>$row['name'],
			'iso'=>$row['iso'],
			'printable_name'=>$row['printable_name'],
			'iso3'=>$row['iso3'],
			'numcode'=>$row['numcode']
		),
	);
	$jsonData['rows'][] = $entry;
}
echo json_encode($jsonData);