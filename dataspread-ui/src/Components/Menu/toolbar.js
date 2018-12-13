import React, {Component} from 'react';
import {Dropdown, Menu} from 'semantic-ui-react'
import ModalAboutUs from './about'
import ModalOpenFile from './load'
import ModalImportFile from './import'
import HierarchiForm from ".././HierarchiForm";
const favstyle = {
    height: '1.5em',
    width: '1.5em',
  };
  
export default class Toolbar extends Component {
    constructor(props) {
        super(props)
        this.handler = this.handler.bind(this)
        this.handleNav = this.handleNav.bind(this)
        this.handleHier = this.handleHier.bind(this)
    }

    handler(e) {
        e.preventDefault()
        this.setState({
            open: false
        })
    }

    handleNav (e) {
        this.props.onNavFormOpen();
    }

    handleHier(e) {
        this.props.onHierFormSub();
    }


    render() {
      return (
        <Menu size='mini' className='toolbar'>
            <Menu.Item>
                <img src='favicon.ico' style={favstyle} alt='DS'/>
            </Menu.Item>

            <Dropdown item text='File'>
                <Dropdown.Menu>
                    <Dropdown.Item>New</Dropdown.Item>
                    <ModalOpenFile {...this.props} handler = {this.handler} />
                    <ModalImportFile/>
                </Dropdown.Menu>
            </Dropdown>

            <Dropdown item text='Edit'>
                <Dropdown.Menu>
                    <Dropdown.Item>Copy</Dropdown.Item>
                    <Dropdown.Item>Paste</Dropdown.Item>
                </Dropdown.Menu>
            </Dropdown>

            <Dropdown item text='Nav'>
                <Dropdown.Menu>
                    <Dropdown.Item onClick={this.handleNav}>Explore</Dropdown.Item>
                    <HierarchiForm grid = {this.props.grid}/>
                </Dropdown.Menu>
            </Dropdown>
            <Dropdown item text='Help'>
                <Dropdown.Menu>
                    <ModalAboutUs/>
                </Dropdown.Menu>
            </Dropdown>


            <Menu.Menu position='right'>
                <Menu.Item>
                    Sign In
                </Menu.Item>
            </Menu.Menu>
        </Menu>
      )
    }
  }




                