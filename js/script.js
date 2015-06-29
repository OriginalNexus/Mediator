window.onscroll=function(){
	var myHeader = document.getElementsByClassName("header")[0]
	if (document.body.scrollTop > 1 && !myHeader.classList.contains("small")) {
		myHeader.classList.add("small");
	}
	else if (document.body.scrollTop <= 1 && myHeader.classList.contains("small")){
		myHeader.classList.remove("small");
	}
};