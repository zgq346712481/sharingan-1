import React from 'react';
import { Table, Form, Row, Col, Input, Popover, Button, message } from 'antd';
import moment from 'moment';
import 'moment/locale/zh-cn';
import InvokeForm from './InvokeForm';
import request from '../utils/request';

const FormItem = Form.Item;
moment.locale('zh-cn');

// 报告列表
const reportColumns = [{
  title: '请求次数',
  dataIndex: 'totalInvoke',
}, {
  title: '响应次数',
  dataIndex: 'responseInvoke',
}, {
  title: '成功比率',
  dataIndex: 'successRate',
  render(data) {
    let color = 'orange'
    if(data === 1.0) {
      color = 'green'
    }
    if(data === 0.0) {
      color = 'red'
    }
    return <div style={{color: color}}> {data} </div>
  }
}, {
  title: '最小耗时',
  dataIndex: 'minResponseTime',
}, {
  title: '最大耗时',
  dataIndex: 'maxResponseTime',
}, {
  title: '平均耗时',
  dataIndex: 'avgResponseTime',
}, {
  title: '开始时间',
  dataIndex: 'dateCreate',
  render(data) {
    return <div> {moment(data).format("YYYY-MM-DD HH:mm:ss")} </div>
  }
}];

// 结果列表
const dataColumns = [{
  title: '请求参数',
  dataIndex: 'realArgs',
  width: '22%',
  render(data) {
    if(data && data.length > 200) {
      return (
        <div>
          {data.substring(0, 250)}
          <Popover placement="topRight" trigger="hover" content={data}>
            <Button>详细</Button>
          </Popover>
        </div>
      )
    }
    return <div style={{paddingLeft: 10}}> {data} </div>
  }
}, {
  title: '结果码',
  dataIndex: 'code',
  width: '6%',
  render(data) {
    if(data === 200) {
      return data
    }
    if(data === 504) {
      return <div style={{color: 'orange'}}> {data} </div>
    }
    return <div style={{color: 'red'}}> {data} </div>
  }
}, {
  title: '请求结果',
  dataIndex: 'result',
  width: '20%',
  render(data) {
    if(data && data.length > 255) {
      return (
        <div>
          {data.substring(0, 250)}
          <Popover placement="topRight" trigger="hover" content={data}>
            <Button>详细</Button>
          </Popover>
        </div>
      )
    }
    return data
  }
}, {
  title: '异常信息',
  dataIndex: 'errorMsg',
  width: '20%',
  render(data) {
    if(data && data.length > 255) {
      return (
        <div>
          {data.substring(0, 250)+`...`}
          <Popover placement="topRight" trigger="hover" content={data}>
            <Button>详细</Button>
          </Popover>
        </div>
      )
    }
    return data
  }
}, {
  title: '耗时',
  dataIndex: 'responseTime',
  width: '8%',
}, {
  title: '请求时间',
  dataIndex: 'invokeTime',
  width: '12%',
  render(data) {
    return <div> {moment(data).format("YYYY-MM-DD HH:mm:ss")} </div>
  }
}];

class ResultList extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      currentPageIndex: 1,
      currentPageSize: 100,
      loading: false,
      data: [],
      fakerId: null,

      pagination: {
        current: 1,
        pageSize: 100,
        defaultPageSize: 100,
        total: 0,
        onChange: (nextPage, pageSize) => {
          this.loadData(this.state.fakerId, nextPage, pageSize);
        },
      },
    }
  }

  // 查询分页信息
  loadData(fakerId, pageIndex, pageSize) {
    const payload = {fakerId: fakerId, pageIndex: pageIndex, pageSize: pageSize };
    this.setState({loading: true})

    //  查询调用结果
    request('api/getResult.json', payload)
      .then(({data, err}) => {
        if(err) {
          message.error(err)
          this.setState({loading: false})
          return
        }

        let dataSource = data.data.map(item =>
          ({
            key: item.id,
            realArgs: item.realArgs,
            code: item.code,
            result: item.result,
            errorMsg: item.errorMsg,
            responseTime: item.responseTime,
            invokeTime: item.invokeTime,
          })
        )

        // 设置分页
        this.state.pagination.current = data.pageIndex
        this.state.pagination.total = data.total

        this.setState({
          data: dataSource,
          loading: false,
          currentPageIndex: data.pageIndex,
          currentPageSize: data.pageSize,
        });
      })
  }

  // 提交查询
  handleSearch = (e) => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if(err) {
        return
      }
      const fakerId = values.fakerId
      if(fakerId === null) {
        message.error('请输入报告编号!')
        return
      }

      // 查询报告信息
      request('api/getReport.json', {fakerId: fakerId})
        .then(({data, err}) => {
          if(err) {
            message.error(err)
            return
          }

          this.state.report = [data.data]
        })

      this.state.fakerId = fakerId
      this.loadData(fakerId, 1, 100)
    });
  }

  render() {
    const { getFieldDecorator } = this.props.form;

    const formItemLayout = {
      labelCol: { span: 9 },
      wrapperCol: { span: 15 },
    };

    const formItemRowLayout = {
        labelCol: {span: 6},
        wrapperCol: {span: 18},
      }
    ;

    return (
      <div>
        <InvokeForm />
        <Form
          className="ant-advanced-search-form"
          style={{paddingLeft: 20, marginTop: 30}}
          onSubmit={this.handleSearch}
        >
          <Row>
            <Col span={12} key='fakerId'>
              <FormItem {...formItemRowLayout} style={{ marginRight: '100px' }} label={`报告编号`}>
                {getFieldDecorator(`fakerId`,
                  {
                  initFieldsValue: null,
                  rules: [{ required: true, message: '请输入报告编号!', whitespace: false}]
                })(
                  <Input
                  />
                )}
              </FormItem>
            </Col>
            <Col span={4} style={{ textAlign: 'right', paddingRight: '50px', marginBottom: 10 }}>
              <Button type="primary" htmlType="submit">查询</Button>
            </Col>
          </Row>
        </Form>
        <Table
          dataSource={this.state.report}
          columns={reportColumns}
          pagination={false}
        />
        <Table
          loading={this.state.loading}
          dataSource={this.state.data}
          columns={dataColumns}
          pagination={this.state.pagination}
        />
      </div>
    );
  }
}

export default Form.create()(ResultList);
