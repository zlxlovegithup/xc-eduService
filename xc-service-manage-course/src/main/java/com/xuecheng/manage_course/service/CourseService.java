package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    /**
     * 查询课程计划
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    /**
     * 添加节点到数据库中
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
       if(teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())){
           //非法参数
           ExceptionCast.cast(CommonCode.INVALID_PARAM);
       }
       //获取课程id
       String courseId = teachplan.getCourseid();
       //获取父节点的id
       String parentId = teachplan.getParentid();
       if(StringUtils.isEmpty(parentId)){
           //获取课程的根节点(一级节点/大章节)
           parentId = getTeachplanRoot(courseId);
       }
       //查询根节点信息
        Optional<Teachplan> optional = teachplanRepository.findById(parentId);
        if(!optional.isPresent()){
            return null;
        }
        Teachplan teachplan1 = optional.get();
        //父节点的级别
        String parent_grade = teachplan1.getGrade();

        //创建一个新的节点准备添加
        Teachplan teachplanNew = new Teachplan();
        //将teachplan的属性拷贝到teachplanNew中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        //设置必要的属性
        teachplanNew.setParentid(parentId);
        if(parent_grade.equals("1")){
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        teachplanNew.setStatus("0"); //未发布

        //将新节点保存到数据库中
        teachplanRepository.save(teachplanNew);

        // 操作成功
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取课程的根节点(一级节点)
     * @param courseId
     * @return
     */
    public String getTeachplanRoot(String courseId){
        //根据课程id(courseId)去数据库中的表course_base查询对应的课程
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        //取出查询到的课程
        CourseBase courseBase = optional.get();

        //课程名称parentId=0 有大章节parentId=1 和小章节parentId=2
        //调用dao去查询teachplan表得到该课程的根节点(一级节点)   (其实就是课程名称)
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        //如果没有查询到课程名称,就创建一个课程(课程名称为根节点)
        if(teachplanList == null || teachplanList.size() <= 0){
            //新添加一个课程的根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setGrade("1"); //一级节点
            teachplan.setStatus("0"); //未上架
            teachplan.setPname(courseBase.getName()); //父节点名称
            teachplanRepository.save(teachplan);
            //返回根节点(一级节点)的id
            return teachplan.getId();
        }
        //返回根节点(一级节点)的id
        return teachplanList.get(0).getId();
    }

    /**
     * 分页查询课程
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId,int page, int size, CourseListRequest courseListRequest){
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //将公司id参数传入dao
        courseListRequest.setCompanyId(companyId);
        if(page <= 0){
            page = 0;
        }
        if(size <=0 ){
            size = 20;
        }
        //设置分页参数
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //查询列表
        List<CourseInfo> courseInfoList = courseListPage.getResult();
        //总记录数
        long total = courseListPage.getTotal();
        //查询结果数
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setList(courseInfoList);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseInfoQueryResult);
    }

    /**
     * 添加课程
     * @param courseBase
     * @return
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        //操作状态/课程ID
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    /**
     * 根据课程id查询课程
     * @param courseid
     * @return
     */
    public CourseBase getCoursebaseById(String courseid){
        //根据课程id去查询课程
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if(optional.isPresent()){
            return optional.get();
        }
        ExceptionCast.cast(CourseCode.CORSE_GET_NOTEXISTS);
        return null;
    }
    /**
     * 先根据课程id查询到课程然后修改课程
     * @param courseid
     * @param courseBase
     * @return
     */
    @Transactional
    public ResponseResult updateCoursebase(String courseid,CourseBase courseBase){
        CourseBase coursebaseById = this.getCoursebaseById(courseid);
        if(coursebaseById == null){
            //抛出异常...
            //非法参数异常
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //修改课程信息
        coursebaseById.setName(courseBase.getName());
        coursebaseById.setMt(courseBase.getMt());//课程的第一级分类
        coursebaseById.setSt(courseBase.getSt());//课程的第二级分类
        coursebaseById.setGrade(courseBase.getGrade());
        coursebaseById.setStudymodel(courseBase.getStudymodel());
        coursebaseById.setUsers(courseBase.getUsers());
        coursebaseById.setDescription(courseBase.getDescription());
        courseBaseRepository.save(coursebaseById);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 通过课程id查询课程营销信息
     * @param courseid
     * @return
     */
    public CourseMarket getCourseMarketById(String courseid){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseid);
        //如果值存在则isPresent()方法会返回true，调用get()方法会返回该对象
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public CourseMarket updateCourseMarket(String courseid,CourseMarket courseMarket){
        CourseMarket courseMarketById = this.getCourseMarketById(courseid);
        if(courseMarketById!=null){
            //可以修改课程信息
            courseMarketById.setCharge(courseMarket.getCharge());
            courseMarketById.setStartTime(courseMarket.getStartTime());
            courseMarketById.setEndTime(courseMarket.getEndTime());
            courseMarketById.setPrice(courseMarket.getPrice());
            courseMarketById.setQq(courseMarket.getQq());
            courseMarketById.setValid(courseMarket.getValid());
            courseMarketRepository.save(courseMarketById);
        }else {
            //添加课程信息
            courseMarketById = new CourseMarket();
            BeanUtils.copyProperties(courseMarket,courseMarketById);
            //设置课程id
            courseMarketById.setId(courseid);
            courseMarketRepository.save(courseMarketById);
        }
        return courseMarketById;
    }

    /**
     * 保存课程图片信息
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId,String pic){
        //根据课程id去查询课程图片信息(去MongoDB数据库)
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        //如果optional存在
        if(optional.isPresent()){
            coursePic = optional.get();
        }

        if(coursePic == null){
            coursePic = new CoursePic();
        }
        //设置实体类CoursePic相关的信息(课程id,pic也就是图片在storage服务器上的存储路径)
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);

        //保存课程图片
        coursePicRepository.save(coursePic);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    public CoursePic findCoursePic(String courseId){
        //根据课程id查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){
        //根据课程id去删除课程图片,删除成功返回true，删除失败返回false
        long result = coursePicRepository.deleteByCourseid(courseId);
        //如果成功删除的话
        if(result>0){
            //删除成功
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //删除失败
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 获取课程详情(课程基础信息,课程图片,课程营销,课程计划)
     * @param courseId
     * @return
     */
    public CourseView getCourseView(String courseId){
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        //如果courseBaseOptional存在的话
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }

        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }

        //查询课程图片信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        if(coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            courseView.setCoursePic(coursePic);
        }

        //查询教学计划信息(通过MyBatis持久层技术查询查询)
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;

    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;

    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;

    @Value("${course-publish.templateId}")
    private String publish_templateId;

    @Value("${course-publish.siteId}")
    private String publish_siteId;

    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    /**
     * 添加课程详情(包含了课程基础信息,课程图片,课程营销,课程计划)
     * @param courseId
     * @return
     */
    public CoursePublishResult preview(String courseId){
        //根据课程id查询课程
        CourseBase courseBase = this.getCoursebaseById(courseId);

        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //课程别名
        cmsPage.setPageAliase(courseBase.getName());
        //课程页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //课程物理路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //获取课程详情(课程基础信息,课程图片,课程营销,课程计划)
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        //1 远程请求CMS服务去保存页面信息(使用到了Feign技术)
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            //操作失败
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //2 获取页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();

        //3 返回CoursePublishResult(当中包含了previewUrl：课程预览的URL地址)
        String pageUrl = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * 一键发布课程页面(先将课程页面存储到MongoDB数据库然后发布页面)
     * @param courseId
     * @return
     * 1:  利用Feign技术远程调用CMS服务去发布课程
     * 2:  获取从CMS服务返回的发布结果
     * 3:  更新课程状态(202002:“已发布”)
     */
    @Transactional
    public CoursePublishResult publish(String courseId){
        //根据课程id查询课程基础信息
        CourseBase one = this.getCoursebaseById(courseId);
        //发布正式课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(courseId);
        if(!cmsPostPageResult.isSuccess()){
            //发布失败
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //更新课程发布状态(将状态码改为202002： ("已发布"))
        CourseBase courseBase = this.saveCoursePublishState(courseId);

        //课程索引...
        //创建课程索引信息(可以使用ElasticSearch技术进行查询课程)
        CoursePub coursePub = this.createCoursePub(courseId);
        //向数据库中保存课程索引信息
        CoursePub newCoursePub = this.saveCoursePub(courseId, coursePub);
        if(newCoursePub == null){
            //创建课程索引信息失败
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }
        //课程缓存...

        //页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(courseId);
        //返回课程发布结果
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    /*
     * 向teachplanMediaPub中保存课程媒资信息
     */
    private void saveTeachplanMediaPub(String courseId) {
        //先删除teachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //从teachplanMedia中查询
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //将teachplanMediaList数据放到teachplanMediaPubs中
        for (TeachplanMedia teaplanMedia:
                teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teaplanMedia,teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        //将teachplanMediaList插入到teachplanMediaPub
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }


    /**
     * 更新课程状态码
     * @param courseId
     */
    private CourseBase saveCoursePublishState(String courseId) {
        //根据课程id查询课程详细信息
        CourseBase courseBase = this.getCoursebaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        //将更改后的课程信息保存到MongoDB数据库中
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    /**
     * 发布正式课程详情页面
     * @param courseId
     * @return
     */
    private CmsPostPageResult publish_page(String courseId) {
        //根据课程id查询课程基础信息
        CourseBase coursebase = this.getCoursebaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点
        cmsPage.setTemplateId(publish_templateId);//模板
        cmsPage.setPageName(courseId+".html");//页面名称
        cmsPage.setPageAliase(coursebase.getName());//页面别名
        cmsPage.setPageWebPath(publish_page_webpath);//页面访问路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面存储路径
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);//数据URL

        //发布页面(远程调用CMS服务的方法postPageQuick (利用FeignClient技术))
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }

    @Autowired
    CoursePubRepository coursePubRepository;

    /**
     * 向数据库中保存课程索引信息
     * @param courseId
     * @param coursePub
     * @return
     */
    public CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
        if (StringUtils.isEmpty(courseId)) {
            //课程id为空
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }

//        CoursePub coursePub0 = coursePub;
//        CoursePub coursePub1 = new CoursePub();
//        coursePub1.setId(coursePub0.getId());
//        coursePub1.setPubTime(coursePub0.getPubTime());

        CoursePub coursePubNew = null;
        //根据课程id获取课程
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
        //coursePubOptional不为空值
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }

        //将给定源bean(coursePub)的属性值复制到目标bean(coursePubNew)中
        //将coursePub中的属性值复制到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);

        //设置主键
        coursePubNew.setId(courseId);
        //将更新时间戳设置为最新时间
        coursePub.setTimestamp(new Date());

        //发布时间
        //时间格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        //设置发布时间
        coursePub.setPubTime(date);
        //保存
        coursePubRepository.save(coursePub);
        return coursePub;
    }

    /**
     * 创建课程索引信息
     * @param courseId
     * @return
     */
    private CoursePub createCoursePub(String courseId){
        CoursePub coursePub = new CoursePub();
        //设置主键
        coursePub.setId(courseId);

        //基础信息 SpringDataJpa技术(下同)
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        //如果courseBaseOptional有值的话
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        if(coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }

        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }

        //课程计划 MyBatis技术
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        //将课程计划转为JSON
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);
        return coursePub;
    }

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    /**
     * 保存媒体资源
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemedia(TeachplanMedia teachplanMedia){
        if(teachplanMedia == null){
            //非法参数
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();

        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            //媒体资源信息为空!
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optional.get();
        //只允许为叶子结点课程计划选择视频
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            //该节点不是叶子节点,请选择叶子节点！
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        TeachplanMedia one = null;
        //查询teachplanMedia
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if(teachplanMediaOptional.isPresent()){
            //有就取出值
            one = teachplanMediaOptional.get();
        }else{
            //没有就新建一个
            one = new TeachplanMedia();
        }

        //将保存媒资信息与课程计划信息 保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        //返回操作结果
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
