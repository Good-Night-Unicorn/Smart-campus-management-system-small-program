
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 学生请假
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/yonghuqingjia")
public class YonghuqingjiaController {
    private static final Logger logger = LoggerFactory.getLogger(YonghuqingjiaController.class);

    private static final String TABLE_NAME = "yonghuqingjia";

    @Autowired
    private YonghuqingjiaService yonghuqingjiaService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private ChatService chatService;//意见反馈
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ExampaperService exampaperService;//试卷表
    @Autowired
    private ExampapertopicService exampapertopicService;//试卷选题
    @Autowired
    private ExamquestionService examquestionService;//试题表
    @Autowired
    private ExamrecordService examrecordService;//考试记录表
    @Autowired
    private ExamredetailsService examredetailsService;//答题详情表
    @Autowired
    private ExamrewrongquestionService examrewrongquestionService;//错题表
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private JiaoshiService jiaoshiService;//教师信息
    @Autowired
    private KechengService kechengService;//课程信息
    @Autowired
    private KechengCollectionService kechengCollectionService;//课程收藏
    @Autowired
    private KechengLiuyanService kechengLiuyanService;//课程留言
    @Autowired
    private NewsService newsService;//公告信息
    @Autowired
    private TongxunluService tongxunluService;//通讯录
    @Autowired
    private TongzhiService tongzhiService;//通知信息
    @Autowired
    private XuexiaojianjieService xuexiaojianjieService;//学校简介
    @Autowired
    private YonghuService yonghuService;//学生
    @Autowired
    private YonghuKaoqinService yonghuKaoqinService;//学生考勤
    @Autowired
    private YonghuKaoqinListService yonghuKaoqinListService;//学生考勤详情
    @Autowired
    private ZuoyeService zuoyeService;//作业
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("学生".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("教师信息".equals(role))
            params.put("jiaoshiId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = yonghuqingjiaService.queryPage(params);

        //字典表数据转换
        List<YonghuqingjiaView> list =(List<YonghuqingjiaView>)page.getList();
        for(YonghuqingjiaView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YonghuqingjiaEntity yonghuqingjia = yonghuqingjiaService.selectById(id);
        if(yonghuqingjia !=null){
            //entity转view
            YonghuqingjiaView view = new YonghuqingjiaView();
            BeanUtils.copyProperties( yonghuqingjia , view );//把实体数据重构到view中
            //级联表 学生
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(yonghuqingjia.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YonghuqingjiaEntity yonghuqingjia, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,yonghuqingjia:{}",this.getClass().getName(),yonghuqingjia.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("学生".equals(role))
            yonghuqingjia.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<YonghuqingjiaEntity> queryWrapper = new EntityWrapper<YonghuqingjiaEntity>()
            .eq("yonghu_id", yonghuqingjia.getYonghuId())
            .eq("yonghuqingjia_name", yonghuqingjia.getYonghuqingjiaName())
            .eq("yonghuqingjia_types", yonghuqingjia.getYonghuqingjiaTypes())
            .eq("yonghuqingjia_number", yonghuqingjia.getYonghuqingjiaNumber())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuqingjiaEntity yonghuqingjiaEntity = yonghuqingjiaService.selectOne(queryWrapper);
        if(yonghuqingjiaEntity==null){
            yonghuqingjia.setInsertTime(new Date());
            yonghuqingjia.setYonghuqingjiaYesnoTypes(1);
            yonghuqingjia.setCreateTime(new Date());
            yonghuqingjiaService.insert(yonghuqingjia);
            return R.ok();
        }else {
            if(yonghuqingjiaEntity.getYonghuqingjiaYesnoTypes()==1)
                return R.error(511,"有相同的待审核的数据");
            else if(yonghuqingjiaEntity.getYonghuqingjiaYesnoTypes()==2)
                return R.error(511,"有相同的审核通过的数据");
            else
                return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YonghuqingjiaEntity yonghuqingjia, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,yonghuqingjia:{}",this.getClass().getName(),yonghuqingjia.toString());
        YonghuqingjiaEntity oldYonghuqingjiaEntity = yonghuqingjiaService.selectById(yonghuqingjia.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("学生".equals(role))
//            yonghuqingjia.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

            yonghuqingjiaService.updateById(yonghuqingjia);//根据id更新
            return R.ok();
    }


    /**
    * 审核
    */
    @RequestMapping("/shenhe")
    public R shenhe(@RequestBody YonghuqingjiaEntity yonghuqingjiaEntity, HttpServletRequest request){
        logger.debug("shenhe方法:,,Controller:{},,yonghuqingjiaEntity:{}",this.getClass().getName(),yonghuqingjiaEntity.toString());

        YonghuqingjiaEntity oldYonghuqingjia = yonghuqingjiaService.selectById(yonghuqingjiaEntity.getId());//查询原先数据

//        if(yonghuqingjiaEntity.getYonghuqingjiaYesnoTypes() == 2){//通过
//            yonghuqingjiaEntity.setYonghuqingjiaTypes();
//        }else if(yonghuqingjiaEntity.getYonghuqingjiaYesnoTypes() == 3){//拒绝
//            yonghuqingjiaEntity.setYonghuqingjiaTypes();
//        }
        yonghuqingjiaEntity.setYonghuqingjiaShenheTime(new Date());//审核时间
        yonghuqingjiaService.updateById(yonghuqingjiaEntity);//审核

        return R.ok();
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<YonghuqingjiaEntity> oldYonghuqingjiaList =yonghuqingjiaService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        yonghuqingjiaService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<YonghuqingjiaEntity> yonghuqingjiaList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            YonghuqingjiaEntity yonghuqingjiaEntity = new YonghuqingjiaEntity();
//                            yonghuqingjiaEntity.setYonghuId(Integer.valueOf(data.get(0)));   //学生 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaName(data.get(0));                    //请假标题 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaText(data.get(0));                    //请假缘由 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaTypes(Integer.valueOf(data.get(0)));   //请假类型 要改的
//                            yonghuqingjiaEntity.setInsertTime(date);//时间
//                            yonghuqingjiaEntity.setYonghuqingjiaTime(sdf.parse(data.get(0)));          //请假时间 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaNumber(Integer.valueOf(data.get(0)));   //请假天数 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaYesnoTypes(Integer.valueOf(data.get(0)));   //申请状态 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaYesnoText(data.get(0));                    //处理意见 要改的
//                            yonghuqingjiaEntity.setYonghuqingjiaShenheTime(sdf.parse(data.get(0)));          //审核时间 要改的
//                            yonghuqingjiaEntity.setCreateTime(date);//时间
                            yonghuqingjiaList.add(yonghuqingjiaEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        yonghuqingjiaService.insertBatch(yonghuqingjiaList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = yonghuqingjiaService.queryPage(params);

        //字典表数据转换
        List<YonghuqingjiaView> list =(List<YonghuqingjiaView>)page.getList();
        for(YonghuqingjiaView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YonghuqingjiaEntity yonghuqingjia = yonghuqingjiaService.selectById(id);
            if(yonghuqingjia !=null){


                //entity转view
                YonghuqingjiaView view = new YonghuqingjiaView();
                BeanUtils.copyProperties( yonghuqingjia , view );//把实体数据重构到view中

                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(yonghuqingjia.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody YonghuqingjiaEntity yonghuqingjia, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,yonghuqingjia:{}",this.getClass().getName(),yonghuqingjia.toString());
        Wrapper<YonghuqingjiaEntity> queryWrapper = new EntityWrapper<YonghuqingjiaEntity>()
            .eq("yonghu_id", yonghuqingjia.getYonghuId())
            .eq("yonghuqingjia_name", yonghuqingjia.getYonghuqingjiaName())
            .eq("yonghuqingjia_text", yonghuqingjia.getYonghuqingjiaText())
            .eq("yonghuqingjia_types", yonghuqingjia.getYonghuqingjiaTypes())
            .eq("yonghuqingjia_number", yonghuqingjia.getYonghuqingjiaNumber())
            .in("yonghuqingjia_yesno_types", new Integer[]{1,2})
            .eq("yonghuqingjia_yesno_text", yonghuqingjia.getYonghuqingjiaYesnoText())
//            .notIn("yonghuqingjia_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YonghuqingjiaEntity yonghuqingjiaEntity = yonghuqingjiaService.selectOne(queryWrapper);
        if(yonghuqingjiaEntity==null){
            yonghuqingjia.setInsertTime(new Date());
            yonghuqingjia.setYonghuqingjiaYesnoTypes(1);
            yonghuqingjia.setCreateTime(new Date());
        yonghuqingjiaService.insert(yonghuqingjia);

            return R.ok();
        }else {
            if(yonghuqingjiaEntity.getYonghuqingjiaYesnoTypes()==1)
                return R.error(511,"有相同的待审核的数据");
            else if(yonghuqingjiaEntity.getYonghuqingjiaYesnoTypes()==2)
                return R.error(511,"有相同的审核通过的数据");
            else
                return R.error(511,"表中有相同数据");
        }
    }

}

