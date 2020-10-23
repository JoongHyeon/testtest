<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<style type="text/css">
	#boardDetailTable{width: 800px; margin: auto; border-collapse: collapse; border-left: hidden; border-right: hidden;}
	#boardDetailTable tr td{padding: 5px;}
	
	.replyTable{margin: auto; width: 500px;}
</style>
</head>
<body>
	<c:import url="../common/menubar.jsp"/>
	
	<h1 align="center">${ board.bId }번 글 상세보기</h1>
	
	<table border="1" id="boardDetailTable">
		<tr>
			<th>번호</th>
			<td>${ board.bId }</td>
		</tr>
		<tr>
			<th>제목</th>
			<td>${ board.bTitle }</td>
		</tr>
		<tr>
			<th>작성자</th>
			<td>${ board.bWriter }</td>
		</tr>
		<tr>
			<th>작성날짜</th>
			<td>${ board.bCreateDate }</td>
		</tr>
		<tr>
			<th>내용</th>
			<%-- <td>${ board.bContent }</td> --%>
			<!-- 
				이렇게만 두면 엔터가 먹지 않음. 
				DB에는 엔터가 \r\n으로 들어가서 이를 치환해주는 작업 필요
			-->
			
			<% pageContext.setAttribute("newLineChar", "\r\n"); %> <!-- \r\n 말고 그냥 \n도, \r도 가능하다 -->
			<td>${ fn:replace(board.bContent, newLineChar, "<br>") }</td>
		</tr>
		
		<c:if test="${ !empty board.originalFileName }">
			<tr>
				<th>첨부파일</th>
				<td>
					<a href="${ contextPath }/resources/buploadFiles/${ board.renameFileName }" download="${ board.originalFileName }">${ board.originalFileName }</a>
					<!-- a태그 안에서 다운로드 받을 것이 있을 때 쓰는 속성 download, 얘는 download="fileName" 이라고 해서 fileName을 지정해줄 수 있다. -->
				</td>
			</tr>
		</c:if>
		
		<c:url var="bupView" value="bupView.bo">
			<c:param name="bId" value="${ board.bId }"/>
			<c:param name="page" value="${ page }"/>
		</c:url>
		<c:url var="bdelete" value="bdelete.bo">
			<c:param name="bId" value="${ board.bId }"/>
		</c:url>
		<c:url var="blist" value="blist.bo">
			<c:param name="page" value="${ page }"/>
		</c:url>
		
		<c:if test="${ loginUser.id eq board.bWriter }">
		<tr>
			<td colspan="2" align="center">
				<button onclick="location.href='${ bupView }'">수정하기</button>
				<button onclick="location.href='${ bdelete }'">삭제하기</button>
			</td>
		</tr>
		</c:if>
		
	</table>
	
	<p align="center">
		<button onclick="location.href='home.do'">시작 페이지로 이동</button>
		<button onclick="location.href='${ blist }'">목록 보기로 이동</button>
	</p>
	
	<br><br>
	
	<table class="replyTable">
		<tr>
			<td>
				<textarea rows="3" cols="55" id="rContent"></textarea>
			</td>
			<td>
				<button id="rSubmit">등록하기</button>
			</td>
		</tr>
	</table>
	
	<table class='replyTable' id="rtb">
		<thead>
			<tr>
				<td colspan="2"><b id="rCount"></b></td>
			</tr>
		</thead>
		<tbody></tbody>	
	</table>
	
	<script>
	
	$(function(){
		getReplyList();
		setInterval(function(){
			getReplyList();
		}, 1000);
	});
		$('#rSubmit').on('click', function(){
				var rContent; //댓글 내용
				var refBid; //댓글이 달린 게시글 번호
				
				rContent =$('#rContent').val();
				refBid = ${ board.bId };
				$.ajax({
					url: 'addReply.bo',
					data : { rContent:rContent, refBid:refBid},
					success : function(data){
						console.log(data);
						
				}
			});
		});
		
	function getReplyList(){
		var bId = ${ board.bId };
		
		$.ajax({
			url: "rList.bo",
			data : {bId:bId},
			success : function(data){
				console.log(data);
				
				$tableBody = $('#rtb tbody');
				$tableBody.html('');
				
				var $tr;
				var $rWriter;
				var $rContent;
				var $rCreateDate;
				
				$('#rCount').text('댓글(' + data.length + ')');
				
				if(data.length > 0){
					console.log(data.length);
					for(var i in data){
						$tr = $('<tr>');
						$rWriter = $('<td width=100>').text(data[i].rWriter);
						$rContent = $('<td>').text(data[i].rContent);
						$rCreateDate = $('<td width=100>').text(data[i].rCreateDate);
						
						$tr.append($rWriter);
						$tr.append($rContent);
						$tr.append($rCreateDate);
						$tableBody.append($tr);
					}
				}else{
					$tr = $('<tr>');
					$rContent = $('<td colspan="3">').text("등록된 댓글이 없습니다.");
					
					$tr.append($rContent);
					$tableBody.append($tr);
				}
			}
		});
	}
	</script>
</body>
</html>