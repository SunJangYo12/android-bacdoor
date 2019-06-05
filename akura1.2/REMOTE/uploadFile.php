 <?php

unlink("payloadout/".$_FILES["filUpload"]["name"]);

if (move_uploaded_file($_FILES["filUpload"]["tmp_name"],"payloadout/".$_FILES["filUpload"]["name"])) {
	$arr["StatusID"] = "1"; 	
 	$arr["Error"] = ""; 
} 
else { 	
	$arr["StatusID"] = "0"; 	
	$arr["Error"] = "Cannot upload file.";
}  
echo json_encode($arr); 

?>