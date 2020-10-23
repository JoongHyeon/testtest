package com.kh.spring.board.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.kh.spring.board.model.exception.BoardException;
import com.kh.spring.board.model.service.BoardService;
import com.kh.spring.board.model.vo.Board;
import com.kh.spring.board.model.vo.PageInfo;
import com.kh.spring.board.model.vo.Reply;
import com.kh.spring.common.Pagination;
import com.kh.spring.member.model.vo.Member;

@Controller
public class BoardController {
	
	@Autowired
	private BoardService bService;
	
	@RequestMapping("blist.bo")
		public ModelAndView boardList(@RequestParam(value="page", required=false) Integer page, ModelAndView mv) {//int라고 하면 null과 비교할수가 없음
																//required=false : 값이 있을 수도 있고 없을 수도 있다	
			int currentPage = 1;
			if(page != null) {
				currentPage = page;
			}
			
			int listCount = bService.getListCount();
			
			PageInfo pi = Pagination.getPageInfo(currentPage, listCount);
			
			ArrayList<Board> list = bService.selectList(pi);
			
			
			if(list != null) {
				mv.addObject("list", list);
				mv.addObject("pi", pi);
				mv.setViewName("boardListView");
			}else {
				throw new BoardException("게시판 전체 조회에 실패하였습니다.");
			}
			return mv;
	}
	
	
	@RequestMapping("binsertView.bo")
	public String boardInserView() {
		return "boardInsertForm";
	}
	
	@RequestMapping("binsert.bo")
	public String boardInsert(@ModelAttribute Board b, @RequestParam("uploadFile") MultipartFile uploadFile, HttpServletRequest request) {
//		System.out.println(b);
//		System.out.println(uploadFile);
		
//		System.out.println(uploadFile.getOriginalFilename());
		// 파일을 넣지 않음 ==> ""
		// 파일을 넣음 ==> 파일 제목
		
		//if(!uploadFile.getOriginalFilename().equals(""){
		if(uploadFile != null && !uploadFile.isEmpty()) {
			// saveFile() : 파일을 저장할 경로 지정
			String renameFileName = saveFile(uploadFile, request);
			
			if(renameFileName != null) {
				b.setOriginalFileName(uploadFile.getOriginalFilename());
				b.setRenameFileName(renameFileName);
			}
		}
		
		int result = bService.insertBoard(b);
		
		return "redirect:blist.bo";
	}
	
	public String saveFile(MultipartFile file, HttpServletRequest request) {
		String root = request.getSession().getServletContext().getRealPath("resources");
//		System.out.println(root);
		
		String savePath = root + "\\buploadFiles";
		
		File folder = new File(savePath);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String originFileName = file.getOriginalFilename();
		String renameFileName = sdf.format(new Date(System.currentTimeMillis()))
								+ "." + originFileName.substring(originFileName.lastIndexOf(".") + 1); 
		
		String renamePath = folder + "\\" + renameFileName;
		
		try {
			file.transferTo(new File(renamePath));
		} catch (Exception e) {
			System.out.println("파일 전동 에러 : " + e.getMessage());
			e.printStackTrace();
		}
		
		return renameFileName;
	}
	
	@RequestMapping("bdetail.bo")
	public ModelAndView boardDetail(@RequestParam("bId") int bId, @RequestParam("page") int page, ModelAndView mv) {
		
		Board board = bService.selectBoard(bId);
		
		if(board != null) {
		mv.addObject("board", board)
		 .addObject("page", page)
		 .setViewName("boardDetailView");
		} else {
			throw new BoardException("게시글 상세보기에 실패하였습니다.");
		}
		
		return mv;
	}
	
	
	@RequestMapping("bupView.bo")
	public ModelAndView boardUpdateView(@RequestParam("bId") int bId, @RequestParam("page") int page, ModelAndView mv) {
		Board board = bService.selectBoard(bId);
		
		mv.addObject("board", board)
		  .addObject("page",page)
		  .setViewName("boardUpdateForm");
		
		return mv;
	}
	
	@RequestMapping("bupdate.bo")
	public ModelAndView boardUpdate(@ModelAttribute Board b, @RequestParam("page") int page,
							  @RequestParam("reloadFile") MultipartFile reloadFile, HttpServletRequest request, ModelAndView mv) {
		
		System.out.println("b.getRenameFileName() : "  + b.getRenameFileName());
		System.out.println("reloadFile : "  + reloadFile);
		if(reloadFile != null && !reloadFile.isEmpty()) {
			if(b.getRenameFileName() != null) {
				deleteFile(b.getRenameFileName(), request);
			}
			
			String renameFileName = saveFile(reloadFile, request);
			
			if(renameFileName != null) {
				b.setOriginalFileName(reloadFile.getOriginalFilename());
				b.setRenameFileName(renameFileName);
			}
			
		}
		
		int result = bService.updateBoard(b);
		
		if(result > 0) {
			mv.addObject("page",page)
			  .setViewName("redirect:bdetail.bo?bId=" + b.getbId());
		}else {
			throw new BoardException("게시글 수정에 실패하였습니다.");
		}
		
		return mv;	
	}
	
	public void deleteFile(String fileName, HttpServletRequest request) {
		String root = request.getSession().getServletContext().getRealPath("resources");
		String savePath = root + "\\buploadFiles";
		
		File f = new File(savePath + "\\" + fileName);
		
		if(f.exists()) {
			f.delete();
		}
	}
	
	@RequestMapping("bdelete.bo")
	public String deleteBoard(@RequestParam("bId") int bId, HttpServletRequest request) {
		
		Board b = bService.selectBoard(bId);
		
		if(b.getOriginalFileName() != null){
			deleteFile(b.getRenameFileName(), request);
		}
		
		int  result = bService.deleteBoard(bId);
		
		if(result > 0) {
			return "redirect:blist.bo";
		} else {
			throw new BoardException("게시물 삭제에 실패하였습니다.");		}
	}
	
	@RequestMapping("addReply.bo")
	@ResponseBody
	public String addReply(Reply r, HttpSession session) {
		
		Member loginUser = (Member)session.getAttribute("loginUser");
		String rWriter = loginUser.getId();
		
		r.setrWriter(rWriter);
				
		int result = bService.addReply(r);
		
		if(result > 0) {
			return "success";
		}else {
			throw new BoardException("댓글 등록에 실패하였습니다.");
		}
	}
	
	
	@RequestMapping("rList.bo")
	public void getReplyList(int bId, HttpServletResponse response) throws JsonIOException, IOException {
		
		ArrayList<Reply> list = bService.selectReplyList(bId);
		
		response.setContentType("application/json; charset=UTF-8");
		
		GsonBuilder gb = new GsonBuilder();
		GsonBuilder df = gb.setDateFormat("yyyy-MM-dd");
		Gson gson = df.create();
		gson.toJson(list, response.getWriter());
	}
	
	@RequestMapping("topList.bo")
	public void boardTopList(HttpServletResponse response) throws JsonIOException, IOException {
		ArrayList<Board> list = bService.selectTopList();
		
		response.setContentType("apllication/json; charset=UTF-8");
		
		new GsonBuilder().setDateFormat("yyy-MM-dd").create().toJson(list,response.getWriter());
	}
}
